package qj.blog.classreloading.example5.reloadable.dao;

import qj.blog.classreloading.example5.reloadable.model.Contact;
import qj.tool.sql.Builder;
import qj.tool.sql.Template;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Quan on 22/12/2014.
 */
public class ContactDAO {
	static Template<Contact> template = new Builder<>(Contact.class)
			.build();

	public static List<Contact> selectAll(Connection conn) {
		return template.selectAll(conn);
	}

    public static void insert(Contact contact, Connection conn) {
        template.insert(contact, conn);
    }
}
