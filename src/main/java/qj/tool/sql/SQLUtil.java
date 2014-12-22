package qj.tool.sql;

import qj.util.IOUtil;
import qj.util.ReflectUtil;
import qj.util.StringUtil;
import qj.util.funct.P0;
import qj.util.funct.P3;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class SQLUtil {

	public static void transaction(P0 run, Connection conn) {
		try {
			conn.setAutoCommit(false);
			run.e();
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			try {
				conn.rollback();
				conn.setAutoCommit(true);
			} catch (SQLException ignored) {
			}
			throw new RuntimeException(e);
		}
	}
	public static Long selectLong(Connection conn, String query, Object... params) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			psSet1(params, ps);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong(1);
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(rs);
			IOUtil.close(ps);
		}
	}
	public static void update(Connection conn, String query, Object... params) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(query);
			psSet1(params, ps);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
	}
	public static P3<PreparedStatement, Integer, Object> setter(Class<?> type) {
		if (type.equals(Date.class)) {
			return (ps, index, val) -> {
				try {
					if (val==null) {
						ps.setNull(index, Types.DATE);
					} else {
						ps.setTimestamp(index, new Timestamp(((Date)val).getTime()));
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			};
		}
		if (type.equals(boolean.class)) {
			return (ps, index, val) -> {
				try {
					ps.setInt(index, (Boolean) val ? 1 : 0);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			};
		}
		if (type.equals(byte[].class)) {
			return (ps, index, val) -> {
				try {
					ps.setBlob(index, new SerialBlob((byte[])val));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			};
		}
		String methodName = "set" + StringUtil.upperCaseFirstChar(type.getSimpleName());
		Method method = ReflectUtil.findMethod(PreparedStatement.class, (m) -> m.getName().equals(methodName) && m.getParameterCount() == 2);
		if (method == null) {
			return null;
		}
		return (ps, index, val) -> {
			
			if (val==null) {
				try {
					ps.setNull(index, Types.VARCHAR);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			} else {
//				System.out.println(method.getName());
				ReflectUtil.invoke(method, ps, index, val);
			}
		};
	}
	static int psSet(Object[] params, PreparedStatement ps, int index)
			throws SQLException {
		if (params == null) {
			return index;
		}
		for (Object val : params) {
			if (val == null) {
				ps.setNull(index++, Types.INTEGER);
			} else {
				P3<PreparedStatement, Integer, Object> setter = setter(val.getClass());
				setter.e(ps, index++, val);
			}
		}
		return index;
	}
	static void psSet1(Object[] params, PreparedStatement ps) throws SQLException {
		psSet(params, ps, 1);
	}
	public static int execute(String query, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(query);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(ps);
		}
	}
}
