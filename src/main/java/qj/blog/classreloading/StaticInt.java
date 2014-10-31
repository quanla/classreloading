package qj.blog.classreloading;

import qj.util.ReflectUtil;
import qj.util.lang.CompositeClassLoader;

/**
 * Created by Quan on 26/10/2014.
 */
public class StaticInt {
	public static void main(String[] args) throws NoSuchFieldException {
		Class<?> userClass = new CompositeClassLoader("target/classes").load("qj.blog.classreloading.StaticInt$User");
		ReflectUtil.setFieldValue(11, userClass.getDeclaredField("age"), null);

		System.out.println("Seems to be the same class:");
		System.out.println(userClass.getName());
		System.out.println(User.class.getName());
		System.out.println();

		System.out.println("But why there are 2 different class loaders:");
		System.out.println(userClass.getClassLoader());
		System.out.println(User.class.getClassLoader());
		System.out.println();

		System.out.println("And different ages:");
		System.out.println(User.age);
		System.out.println((Integer)ReflectUtil.getFieldValue(userClass.getDeclaredField("age"), null));
	}

	public static class User {
		public static int age = 10;
	}
}
