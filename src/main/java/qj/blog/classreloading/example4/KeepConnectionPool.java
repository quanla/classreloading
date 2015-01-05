package qj.blog.classreloading.example4;

import static qj.util.ReflectUtil.*;

import qj.blog.classreloading.example4.crossing.ConnectionPool;
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

	private static Object createContext(ConnectionPool pool) {
		ExceptingClassLoader classLoader = new ExceptingClassLoader(
				(className) -> className.contains(".crossing."),
				"target/classes");
		Class<?> contextClass = classLoader.load("qj.blog.classreloading.example4.reloadable.Context");
		Object context = newInstance(contextClass);
		
		setFieldValue(pool, "pool", context);
		invoke("init", context);

		return context;
	}

	private static void invokeService(Object context) {
		Object hobbyService = getFieldValue("userService", context);
		invoke("hello", hobbyService);
	}
	


}
