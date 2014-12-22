package qj.tool.sql;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import qj.util.Cols;
import qj.util.IOUtil;
import qj.util.ReflectUtil;
import qj.util.RegexUtil;
import qj.util.funct.F1;
import qj.util.funct.F2;
import qj.util.funct.Fs;
import qj.util.funct.P1;
import qj.util.funct.P3;

@SuppressWarnings("UnusedDeclaration")
public class Template<M> {
	Class<M> clazz;
	List<Field1<M>> idFields;
	List<Field1<M>> dataFields;
	String tableName;
	boolean autoIncrement = true;
	
	Template(Class<M> clazz) {
		this.clazz = clazz;
	}


	static abstract class Field1<M> {
		F2<ResultSet,Integer,Object> rsGet = null;
		P3<PreparedStatement, Integer, Object> psSetter = null;
		String sqlName = null;
		Type type;
		abstract void setValue(Object val, M m);
		abstract Object getValue(M m);
	}

	public static <M> Builder<M> builder(Class<M> clazz) {
		return new Builder<>(clazz);
	}

	public M insert(M m, Connection conn) {

		boolean hasId = getId(m) != null;
		List<Field1<M>> fields = hasId ? allFields() : dataFields;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "INSERT INTO `" + tableName + "`(" + fieldNames(fields) + ") VALUES(" + fieldsPH(fields) + ")";
//			System.out.println(sql);
			ps = conn.prepareStatement(sql, Cols.isEmpty(idFields) ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS); // new String[] {"id"}
			psSet1(fields, m, ps);
			int result = ps.executeUpdate();
			if (result != 1) {
				throw new RuntimeException("Failed to insert record into " + tableName + " table");
			}
			
			if (!hasId && Cols.isNotEmpty(idFields)) {
				rs = ps.getGeneratedKeys();
				if (rs.next()){
					setId(m, rs.getLong(1));
				}
			}
			
			return m;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(rs);
			IOUtil.close(ps);
		}
	}
	
	
	public void insert(Collection<M> col, Connection conn) {
		if (Cols.isEmpty(col)) {
			return;
		}
		
		List<Field1<M>> fields = allFields();

		PreparedStatement ps = null;
		try {
			String sql = "INSERT INTO `" + tableName + "`(" + fieldNames(fields) + ") VALUES(" + fieldsPH(fields) + ")";
//			System.out.println(sql);
			ps = conn.prepareStatement(sql, Cols.isEmpty(idFields) ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS); // new String[] {"id"}

			for (M m : col) {
				psSet1(fields, m, ps);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
//		PreparedStatement ps = conn.prepareStatement("");
//		ps.execute

	}


	private void setId(M m, long id) {
		Cols.getSingle(idFields).setValue(id, m);
	}
	public Object getId(M m) {
		Field1<M> idField = Cols.getSingle(idFields);
		return idField != null ? idField.getValue(m) : null;
	}


	private void psSet1(List<Field1<M>> fields, M m, PreparedStatement ps) throws SQLException {
		int index = 1;
		for (Field1 field : fields) {
			Object val = field.getValue(m);

			if (val == null) {
				ps.setNull(index++, Types.INTEGER);
			} else {
				P3<PreparedStatement, Integer, Object> setter = field.psSetter;
				setter.e(ps, index++, val);
			}
		}
	}

	private String fieldsPH(List<Field1<M>> fields) {
		return Cols.join(Cols.createList(fields.size(), () -> "?"), ",");
	}

	private String fieldNames(List<Field1<M>> fields) {
		return Cols.join((Iterable<String>) Cols.yield(fields, (f) -> "`" + f.sqlName + "`"), ",");
	}

	public int delete(Connection conn, String cond, Object... params) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("DELETE FROM `" + tableName + "` " + (cond != null ? cond : ""));
			SQLUtil.psSet1(params, ps);
			
			return ps.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
	}
	
	/**
	 * SET state=? WHERE id=?
	 */
	public int update(Connection conn, String query, Object... params) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE `" + tableName + "` " + query);
			SQLUtil.psSet1(params, ps);
			
			return ps.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
	}

	public int update(M m, Connection conn, String cond, Object... params) {
		return update(m, allFields(), conn, cond, params);
	}

	public int update(M m, List<Field1<M>> fields, Connection conn, String cond, Object... params) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE `" + tableName + "` SET " + psSetUpdate(fields) + " " + cond);
			psSet1(fields, m, ps);
			SQLUtil.psSet(params, ps, fields.size() + 1);

			return ps.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
	}

	private String psSetUpdate(List<Field1<M>> fields) {
		return Cols.join((Iterable<String>)Cols.yield(fields, (f) -> "`" + f.sqlName + "`=?"), ",");
	}
	
	public M selectById(Object id, Connection conn) {
		if (id == null) {
			return null;
		}
		return select(conn, "WHERE `" + Cols.getSingle(idFields).sqlName + "`=?", id);
	}

	public M select(Connection conn, String query, Object... params) {
		Query<M> parseQuery = parseSelectQuery(query);
		
		AtomicReference<M> ret = new AtomicReference<>();
		
		F1<M, Boolean> f1 = Fs.f1(Fs.setter(ret), true);
		
		String cond = (parseQuery.cond != null ? parseQuery.cond : "") + " LIMIT 1";
		
		each(f1, conn, parseQuery.fields, cond, params);
		return ret.get();
	}

	public List<M> selectAll(Connection conn) {
		List<Field1<M>> fields = allFields();
		String cond = "";
		return selectList(conn, fields, cond, null);
	}

	private List<M> selectList(Connection conn, List<Field1<M>> fields,
	                           String cond, Object[] params) {
		LinkedList<M> list = new LinkedList<>();
		
		each(Fs.store(list), conn, fields, cond, params);
		
		return list;
	}
	
	private void each(P1<M> p1, Connection conn, List<Field1<M>> fields,
			String cond, Object[] params) {
		each(Fs.f1(p1, false), conn, fields, cond, params);
	}

	private void each(F1<M,Boolean> f1, Connection conn, List<Field1<M>> fields,
			String cond, Object[] params) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT " + fieldNames(fields) + " FROM `" + tableName + "`" + (cond == null ? "" : " " + cond);
		try {
			ps = conn.prepareStatement(sql);
			SQLUtil.psSet1(params, ps);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				M m = ReflectUtil.newInstance(clazz);
				rsSet(fields, m, rs);
				if (f1.e(m)) {
					break;
				}
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e + ", sql=" + sql, e);
		} finally {
			IOUtil.close(rs);
			IOUtil.close(ps);
		}
	}

	private void rsSet(List<Field1<M>> fields, M m, ResultSet rs) {
		int index = 1;
		for (Field1 field : fields) {
			field.setValue(field.rsGet.e(rs, index++), m);
		}
	}

	private List<Field1<M>> allFields() {
		LinkedList<Field1<M>> ret = new LinkedList<>();
		ret.addAll(idFields);
		ret.addAll(dataFields);
		return ret;
	}

	public List<M> selectList(Connection conn, String query, Object... params) {
		Query<M> parseQuery = parseSelectQuery(query);
		
		return selectList(conn, parseQuery.fields, parseQuery.cond, params);
	}
	
	private Query<M> parseSelectQuery(String query) {
		if (query == null) {
			return new Query<>(allFields(), null);
		}
		Matcher matcher = RegexUtil.matcher("^(?i)(?:SELECT (.+?) *)?(?:FROM .+? *)?((?:WHERE .+)?(?:ORDER BY .+)?)$", query);
		if (!matcher.matches()) {
			throw new RuntimeException("Can not parse this query: " + query);
		}
		
		return new Query<>(parseFields(matcher.group(1)), matcher.group(2));
	}
//	
//	public static void main(String[] args) {
//		new Template<>(Aa.class).parseSelectQuery("SELECT receiver_name, receiver_address, receiver_phone WHERE order_by=? AND deliver_time > ? ORDER BY deliver_time DESC");
//	}

	private List<Field1<M>> parseFields(String fields) {
		if (fields == null) {
			return allFields();
		}
		LinkedList<Field1<M>> ret = new LinkedList<>();
		for (String sqlName : fields.split("\\s*,\\s*")) {
			ret.add(getField(sqlName));
		}
		return ret;
	}

	private Field1<M> getField(String sqlName) {
		for (Field1<M> field1 : idFields) {
			if (field1.sqlName.equals(sqlName)) {
				return field1;
			}
		}
		for (Field1<M> field1 : dataFields) {
			if (field1.sqlName.equals(sqlName)) {
				return field1;
			}
		}
		throw new RuntimeException("Can not find this field: " + sqlName);
	}

	static class Query<M> {
		List<Field1<M>> fields;
		String cond;

		public Query(List<Field1<M>> fields, String cond) {
			this.fields = fields;
			this.cond = cond;
		}
		
	}

	public boolean exists(Connection conn, String cond, Object... params) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT 1 FROM `" + tableName + "` " + cond + " LIMIT 1");
			SQLUtil.psSet1(params, ps);
			rs = ps.executeQuery();
			
			return rs.next();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(rs);
			IOUtil.close(ps);
		}
	}

	public void save(M m, Connection conn) {
		Object id = getId(m);
		if (id != null) {
			int result = update(m, allFields(), conn, "WHERE " + Cols.getSingle(idFields).sqlName + "=?", id);
			if (result != 1) {
				throw new RuntimeException("Failed to update record into " + tableName + " table");
			}

		} else {
			insert(m, conn);
		}
	}
	
	public void each(P1<M> p1, Connection conn) {
		each(p1, conn, null);
	}

	public void each(P1<M> p1, Connection conn,
			String query, Object... params) {
		Query<M> parseQuery = parseSelectQuery(query);
		
		each(p1, conn, parseQuery.fields, parseQuery.cond, params);
	}

	public <IDT> F1<IDT, M> selectByIdF(Connection conn) {
		return id -> selectById(id, conn);
	}

	public void update(M m, Connection conn) {
		int result = update(m, allFields(), conn, "WHERE " + Cols.getSingle(idFields).sqlName + "=?", getId(m));
		if (result != 1) {
			throw new RuntimeException("Failed to update record into " + tableName + " table");
		}
	}

	/**
	 * 
	 * @param fields SET name, age
	 */
	public void update(M m, String fields, Connection conn) {
		List<Field1<M>> fields1 = Cols.yield(Arrays.asList(fields.replaceFirst("(?i)^SET ","").split(",\\s*")), this::getField);
		int result = update(m, fields1, conn, "WHERE " + Cols.getSingle(idFields).sqlName + "=?", getId(m));
		if (result != 1) {
			throw new RuntimeException("Failed to update record into " + tableName + " table");
		}
	}

	public void delete(M m, Connection conn) {
		delete(conn, "WHERE " + Cols.getSingle(idFields).sqlName + "=?", getId(m));
	}

	public void deleteById(Object id, Connection conn) {
		delete(conn, "WHERE " + Cols.getSingle(idFields).sqlName + "=?", id);
	}

	public void deleteAll(Connection conn) {
		delete(conn, null);
	}
	
	public String tableName() {
		return tableName;
	}
}
