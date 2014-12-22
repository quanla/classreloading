package qj.blog.classreloading.example5;

import qj.blog.classreloading.example5.servlet.JadeServlet;
import qj.blog.classreloading.example5.servlet.PersonServlet;
import qj.util.funct.F0;

import java.sql.Connection;

@SuppressWarnings("UnusedDeclaration")
public class Context {
	public String buildVersion;
	public boolean development;
	public F0<Connection> connF;
	
	public JadeServlet jadeServlet = new JadeServlet();
	public PersonServlet personServlet = new PersonServlet();

	public void init(String webLoc) {
		System.out.println("Initializing context.");
		
		jadeServlet.version = buildVersion;
		jadeServlet.init(webLoc);
		
		personServlet.connF = connF;
	}
	
	public void close() {
		System.out.println("Closing context.");
	}
}
