package qj.util;

/**
 * Created by QuanLA
 * Date: Apr 5, 2006
 * Time: 5:46:44 PM
 */
public class ObjectUtil {
	public static boolean equals(Object o1, Object o2) {
		return o1==null ? o2 == null : (o1 == o2 || o1.equals(o2));
	}
}
