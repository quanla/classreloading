package qj.blog.classreloading.example2;

import qj.util.ReflectUtil;
import qj.util.ThreadUtil;
import qj.util.lang.DynamicClassLoader;

/**
 * Created by Quan on 31/10/2014.
 */
public class ReloadingContinuously {
	public static void main(String[] args) {
		for (;;) {
			Class<?> userClass = new DynamicClassLoader("target/classes")
				.load("qj.blog.classreloading.example2.ReloadingContinuously$User");
			ReflectUtil.invokeStatic("hobby", userClass);
			ThreadUtil.sleep(2000);
		}
	}
	
	@SuppressWarnings("UnusedDeclaration")
	public static class User {
		public static void hobby() {
			playFootball(); // Will comment during runtime
//			playBasketball(); // Will uncomment during runtime
		}
		
		// Will comment during runtime
		public static void playFootball() {
			System.out.println("Play Football");
		}
		
		// Will uncomment during runtime
//		public static void playBasketball() {
//			System.out.println("Play Basketball");
//		}
	}
}
