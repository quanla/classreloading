package qj.blog.classreloading.example1;

import static java.lang.System.*;

import qj.util.ReflectUtil;
import qj.util.lang.DynamicClassLoader;

/**
 * Created by Quan on 26/10/2014.
 */
public class StaticInt {
	public static void main(String[] args) {
		Class<?> userClass1 = User.class;
		Class<?> userClass2 = new DynamicClassLoader("target/classes")
				.load("qj.blog.classreloading.example1.StaticInt$User");


		out.println("Seems to be the same class:");
		out.println(userClass1.getName());
		out.println(userClass2.getName());
		out.println();

		out.println("But why there are 2 different class loaders:");
		out.println(userClass1.getClassLoader());
		out.println(userClass2.getClassLoader());
		out.println();

		User.age = 11;
		out.println("And different age values:");
		out.println((int) ReflectUtil.getStaticFieldValue("age", userClass1));
		out.println((int) ReflectUtil.getStaticFieldValue("age", userClass2));
	}

	public static class User {
		public static int age = 10;
	}
}
