package qj.blog.classreloading.example5;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import qj.tool.web.ReloadingWebContext;
import qj.tool.web.ResourceFilter;
import qj.util.PropertiesUtil;
import qj.util.SystemUtil;
import qj.util.ThreadUtil;
import qj.util.funct.F0;
import qj.util.funct.P0;
import qj.util.lang.ExceptingClassLoader;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
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

public class LittlePhoneBookMain {
	public static boolean development = true;
	public static String version = "1.0.0";

	public static void main(String[] args) throws Exception {
        Properties config = PropertiesUtil.loadPropertiesFromFile("data/example5/config.properties");

		startServer(config);
	}
	
	public static void startServer(Properties config) throws Exception {
		final ServerControl webServer = startWebServer(config);

		// Console commands are used to control server
		SystemUtil.onReturn(line -> {
			// Type exit then enter to stop the server
			if ("exit".equals(line)) {
				System.out.print("Stopping web server...");
				webServer.closeF.run();
				System.out.print(" done.");
				System.exit(0);
			}
		});
	}

	public static ServerControl startWebServer(Properties config) throws Exception {
		int port = Integer.parseInt(config.getProperty("web.port"));

		// Create the connection pool in the persisted area
		DbPool dbPool = initDatabase(config);

		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");

		ReloadingWebContext contextLoader = new ReloadingWebContext(
				"qj.blog.classreloading.example5.reloadable.Context",
				() -> ( development ?
						// During development, the dynamic class loader will be used
						new ExceptingClassLoader(
							(className) -> className.startsWith("qj.util"),
							"target/classes"
						) :
						
						// During production, the default class loader will be used
						LittlePhoneBookMain.class.getClassLoader()
				),
				development ? 
						// During development, each time a GET to root URL "/", the dynamic context will be reloaded
						(req) -> req.getMethod().equalsIgnoreCase("GET") && req.getRequestURI().equals("/") : 
						null
		);
		
		// Fields to be set each time the context is reloaded
		contextLoader.setField("development", development);
		contextLoader.setField("buildVersion", version);
		contextLoader.setField("connF", dbPool.connF);
		
		// Method "init" will be called with param "data/example5/web" each time the context is reloaded
		contextLoader.initWith("init", "data/example5/web");
		
		// Method "close" will be called each time context is un-linked ( to create and link to newer context, with 
		// newer classes)
		contextLoader.beforeClose("close");
		
		// The "stubServlet" method will provide "stub" servlets which will retrieve real servlets in the reloaded 
		// context each time a request is served
		servletContext.addServlet( new ServletHolder(contextLoader.stubServlet("jade")), "/");

		servletContext.addServlet( new ServletHolder(wrapServlet(contextLoader.stubServlet("contact"), dbPool.closeThreadConn)),
				"/contact");

		servletContext.addServlet( new ServletHolder(contextLoader.stubServlet("jade")), "*.jade");

		// Serve resources
		ResourceFilter resourceFilter = resourceFilter("data/example5/web");
		servletContext.addFilter(
				new FilterHolder(resourceFilter),
				"/*", EnumSet.<DispatcherType>of(DispatcherType.REQUEST));

		final Server server = new Server(port);
		server.setHandler(servletContext);

		server.start();
		System.out.println("Server started on port " + port);

		final Runnable closeF = () -> {
			System.out.print("Stopping box server...");
			try {
				server.stop();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			dbPool.closePool.e();
			System.out.print(" done.");
		};
		return new ServerControl(closeF);
	}

	private static DbPool initDatabase(Properties config) throws SQLException, ClassNotFoundException {
		DbPool dbPool = new DbPool(config);
		Connection connection = dbPool.connF.e();
		initDb(connection);
		dbPool.closeThreadConn.e();
		return dbPool;
	}

	public static ResourceFilter resourceFilter(String boxWebLoc) {
		return new ResourceFilter(
				req -> null,
				boxWebLoc
				);
	}

	public static class ServerControl {
		Runnable closeF;

		public ServerControl(Runnable closeF) {
			this.closeF = closeF;
		}
	}

	/**
	 * This is pool provide only 1 shared connection to the SQLite memory database
	 */
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
				//noinspection EmptyCatchBlock
				try {
					connection.close();
				} catch (SQLException e1) {
				}
			};
		}

	}

	/**
	 * The SQLite memory db is initialized before use
	 * @throws SQLException
	 */
	private static void initDb(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);  // set timeout to 30 sec.

		statement.executeUpdate("drop table if exists contact");
		statement.executeUpdate("create table contact (id integer, name string, phone string)");
		statement.executeUpdate("insert into contact values(1, 'Andrew King', '0648 6815 1654')");
		statement.executeUpdate("insert into contact values(2, 'William Shakespeare', '0234 5234 3264')");
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
	
//	public static class Build {
//		public static void main(String[] args) {
//			System.out.println(BuildUtil.runCommand(LittlePhoneBookMain.class));
//		}
//	}

}
