package qj.blog.example2;

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
				.load("qj.blog.example2.ReloadingContinuously$User");
			ReflectUtil.invokeStatic("hobby", userClass);
			ThreadUtil.sleep(2000);
		}
	}
	
	@SuppressWarnings("UnusedDeclaration")
	public static class User {
		public static void hobby() {
			playFootball(); // Will comment later
//			playBasketball(); // Will uncomment later
		}
		
		// Will comment later
		public static void playFootball() {
			System.out.println("Play Football");
		}
		
		// Will uncomment later
//		public static void playBasketball() {
//			System.out.println("Play Basketball");
//		}
	}
}
