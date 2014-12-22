package qj.blog.classreloading.example5.dao;

import qj.blog.classreloading.example5.model.Person;
import qj.tool.sql.Builder;
import qj.tool.sql.Template;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Quan on 22/12/2014.
 */
public class PersonDAO {
	static Template<Person> template = new Builder<>(Person.class)
			.build();

	public static List<Person> selectAll(Connection conn) {
		return template.selectAll(conn);
	}
}
