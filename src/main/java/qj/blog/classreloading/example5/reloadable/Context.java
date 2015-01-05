package qj.blog.classreloading.example5.reloadable;

import qj.blog.classreloading.example5.reloadable.servlet.ContactServlet;
import qj.blog.classreloading.example5.reloadable.servlet.JadeServlet;
import qj.util.funct.F0;

import java.sql.Connection;

@SuppressWarnings("UnusedDeclaration")
public class Context {
	public String buildVersion;
	public boolean development;
	public F0<Connection> connF;
	
	public JadeServlet jadeServlet = new JadeServlet();
	public ContactServlet contactServlet = new ContactServlet();

	public void init(String webLoc) {
//		System.out.println("Initializing context.");
		
		jadeServlet.version = buildVersion;
		jadeServlet.init(webLoc);
		
		contactServlet.connF = connF;
	}
	
	public void close() {
//		System.out.println("Closing context.");
	}
}
