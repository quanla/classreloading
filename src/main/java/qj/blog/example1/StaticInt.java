package qj.blog.example1;

import static java.lang.System.*;

import qj.util.ReflectUtil;
import qj.util.lang.DynamicClassLoader;

/**
 * Created by Quan on 26/10/2014.
 */
public class StaticInt {
	public static void main(String[] args) {
		Class<?> userClass = new DynamicClassLoader("target/classes")
				.load("qj.blog.example1.StaticInt$User");


		out.println("Seems to be the same class:");
		out.println(userClass.getName());
		out.println(User.class.getName());
		out.println();

		out.println("But why there are 2 different class loaders:");
		out.println(userClass.getClassLoader());
		out.println(User.class.getClassLoader());
		out.println();

		User.age = 11;
		out.println("And different age values:");
		out.println((int) ReflectUtil.getStaticFieldValue("age", User.class));
		out.println((int) ReflectUtil.getStaticFieldValue("age", userClass));
	}

	public static class User {
		public static int age = 10;
	}
}
