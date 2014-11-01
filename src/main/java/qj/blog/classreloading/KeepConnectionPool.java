package qj.blog.classreloading;

import static qj.util.ReflectUtil.*;

import qj.util.ThreadUtil;
import qj.util.lang.ExceptingClassLoader;

/**
 * Created by Quan on 01/11/2014.
 */
public class KeepConnectionPool {
	public static void main(String[] args) {

		ConnectionPool pool = new ConnectionPool();

		for (;;) {
			Object context = createContext(pool);

			invokeService(context);

			ThreadUtil.sleep(2000);
		}
	}

	private static void invokeService(Object context) {
		Object hobbyService = getFieldValue("userService", context);
		invoke("hello", hobbyService);
	}
	
	private static Object createContext(ConnectionPool pool) {
		ExceptingClassLoader classLoader = new ExceptingClassLoader(
				(className) -> className.contains("$ConnectionPool"),
				"target/classes");
		Class<?> contextClass = classLoader.load(KeepConnectionPool.class.getName() + "$Context");
		Object context = newInstance(contextClass);
		
		setFieldValue(pool, "pool", context);
		invoke("init", context);

		return context;
	}

	@SuppressWarnings("UnusedDeclaration")
	public static class Context {
		public ConnectionPool pool;
		
		public UserService userService = new UserService();
		
		public void init() {
			userService.pool = pool;
		}
	}
	
	public static class UserService {
		ConnectionPool pool;
		
		@SuppressWarnings("UnusedDeclaration")
		public void hello() {
			System.out.println("UserService CL: " + this.getClass().getClassLoader());
			System.out.println("Hi " + pool.getConnection().getUserName());
		}
	}
	
	public static class ConnectionPool {
		Connection conn = new Connection();
		
		public Connection getConnection() {
			return conn;
		}
	}
	
	public static class Connection {
		public String getUserName() {
			System.out.println("Connection CL: " + this.getClass().getClassLoader());
			return "Joe";
		}
	}
}
