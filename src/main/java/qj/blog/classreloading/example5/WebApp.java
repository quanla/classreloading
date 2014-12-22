package qj.blog.classreloading.example5;

import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import qj.tool.build.BuildUtil;
import qj.tool.web.ReloadingWebContext;
import qj.tool.web.ResourceFilter;
import qj.util.*;
import qj.util.funct.F0;
import qj.util.funct.P0;
import qj.util.lang.DynamicClassLoader;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Properties;

public class WebApp {
	public static boolean development = true;
	public static String version = "1.0.0";

	public static void main(String[] args) throws Exception {
		Properties config = PropertiesUtil.loadPropertiesFromFile("data/example5/config.properties");

		startServer(config);
	}
	
	public static class Build {
		public static void main(String[] args) {
			System.out.println(BuildUtil.runCommand(WebApp.class));
		}
	}

	public static void startServer(Properties config) throws Exception {
		final ServerControl webServer = startWebServer(config);

		SystemUtil.onReturn(line -> {
			if ("exit".equals(line)) {
				System.out.print("Stopping web server...");
				webServer.closeF.e();
				System.out.print(" done.");
				System.exit(0);
			} else if ("".equals(line)) {
				webServer.reloadF.e();
			}
		});
	}

	public static ServerControl startWebServer(Properties config) throws Exception {
		int port = Integer.parseInt(config.getProperty("web.port"));

		DbPool dbPool = initDatabase(config);

		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");

		ResourceFilter resourceFilter = resourceFilter("data/example5/web");
		servletContext.addFilter(
				new FilterHolder(resourceFilter),
				"/*", EnumSet.<DispatcherType>of(DispatcherType.REQUEST));

		ReloadingWebContext contextLoader = new ReloadingWebContext(
				"qj.blog.classreloading.example5.Context",
				() -> ( development ?
						// During development, the dynamic class loader will be used
						new DynamicClassLoader(
							"target/classes"
						) :
						
						// During production, the default class loader will be used
						WebApp.class.getClassLoader()
				),
				development ? 
						// During development, each time a GET to root URL "/", the dynamic context will be reloaded
						(req) -> req.getMethod().equalsIgnoreCase("GET") && req.getRequestURI().equals("/") : 
						null
		);
		
		contextLoader.setField("development", development);
		contextLoader.setField("buildVersion", version);
		contextLoader.setField("connF", dbPool.connF);
		contextLoader.initWith("init", "data/example5/web");
		contextLoader.beforeClose("close");
		
		HttpServlet jadeServlet = contextLoader.getServlet("jade");
		servletContext.addServlet( new ServletHolder(jadeServlet), "/");

		servletContext.addServlet( new ServletHolder(wrapServlet(contextLoader.getServlet("person"), dbPool.closeThreadConn)), "/person");

		servletContext.addServlet( new ServletHolder(jadeServlet), "*.jade");

		final Server server = new Server(port);
		server.setHandler(servletContext);

		server.start();
		System.out.println("Server started on port " + port);

		final P0 closeF = () -> {
			System.out.print("Stopping box server...");
			try {
				server.stop();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			dbPool.closePool.e();
			System.out.print(" done.");
		};
		return new ServerControl(closeF, () -> contextLoader.reload());
	}

	private static DbPool initDatabase(Properties config) throws SQLException, ClassNotFoundException {
		DbPool dbPool = new DbPool(config);
		Connection connection = dbPool.connF.e();
		initDb(connection);
		dbPool.closeThreadConn.e();
		return dbPool;
	}

	public static ResourceFilter resourceFilter(String boxWebLoc) {
		return new ResourceFilter(development,
				req -> null,
				boxWebLoc
				);
	}

	public static class ServerControl {
		P0 closeF;
		P0 reloadF;

		public ServerControl(P0 closeF, P0 reloadF) {
			this.closeF = closeF;
			this.reloadF = reloadF;
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	public static void addServlet(String url, HttpServlet servlet, ServletContextHandler servletContext) {
		servletContext.addFilter(
				new FilterHolder(new RegexUriFilter(servlet, "/" + url + "/.+")),
				"/" + url + "/*", EnumSet.<DispatcherType>of(DispatcherType.REQUEST));
	}

	public static class RegexUriFilter implements Filter {

		private String regex;
		private HttpServlet servlet;

		public RegexUriFilter(HttpServlet servlet, String regex) {
			this.servlet = servlet;
			this.regex = regex;
		}

		@Override
		public void destroy() {
		}

		@Override
		public void doFilter(ServletRequest req1, ServletResponse resp,
				FilterChain chain) throws IOException, ServletException {
			HttpServletRequest req = (HttpServletRequest) req1;

			//  && !((HttpServletRequest) req1).getMethod().equals("GET")
			if (RegexUtil.matches(req.getRequestURI(), regex)) {
				servlet.service(req, resp);
				
				if (resp.getContentType() == null) {
					resp.setContentType((req.getMethod().equals("GET")? "text/html" : "application/json") + "; charset=UTF-8");
				}
			} else {
				chain.doFilter(req, resp);
			}
		}

		@Override
		public void init(FilterConfig arg0) throws ServletException {
		}

	}

	static class DbPool {

		public F0<Connection> connF;
		public P0 closeThreadConn;
		protected P0 closePool;

		public DbPool(Properties config) throws SQLException, ClassNotFoundException {
			
			Class.forName(config.getProperty("db.driver"));

            Connection connection = DriverManager.getConnection(config.getProperty("db.url"));

			ThreadUtil.ThreadLocalCache<Connection> threadLocal = ThreadUtil.threadLocalCache(() -> connection);
			connF = threadLocal.cacheF;
			closeThreadConn = () -> {
//				Connection conn = threadLocal.removeF.e();
//				if (conn != null) {
////					System.out.println("Closing thread ds");
//					IOUtil.close(conn);
//				}
			};

			closePool = () -> {
				try {
					connection.close();
				} catch (SQLException e1) {
					throw new RuntimeException(e1);
				}
			};
		}

	}

	private static void initDb(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.

		statement.executeUpdate("drop table if exists person");
		statement.executeUpdate("create table person (id integer, name string)");
		statement.executeUpdate("insert into person values(1, 'leo')");
		statement.executeUpdate("insert into person values(2, 'yui')");
	}


	private static HttpServlet wrapServlet(HttpServlet servlet, P0 closeThreadConn) {
		return new HttpServlet() {
			protected void service(HttpServletRequest req,
			                       HttpServletResponse resp) throws ServletException,
					IOException {
				try {
					servlet.service(req, resp);
				} finally {
					closeThreadConn.e();
				}
			}
		};
	}
}
