package qj.util;

import qj.util.funct.F0;
import qj.util.funct.F1;

import java.util.*;

/**
 * Created by QuanLA
 * Date: Mar 2, 2006
 * Time: 9:10:49 AM
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Cols {

	/**
	 * If a collection is empty
	 * @param objs
	 * @return
	 */
	public static boolean isEmpty(Collection<?> objs) {
		return objs == null || objs.isEmpty();
	}

	/**
	 * If a collection is not empty
	 * @param col
	 * @return
	 */
    public static boolean isNotEmpty(Collection<?> col) {
        return !isEmpty(col);
    }

	
    /**
     * Get single element, or null if Col is empty
     * @param collection
     * @return
     */
    public static <A> A getSingle(
            Collection<A> collection) {
    	if (collection == null) {
    		return null;
    	}
        for (A a : collection) {
            return a;
        }
        return null;
    }

	
    public static <A> List<A> createList(int size, F0<A> f) {
        ArrayList<A> list = new ArrayList<A>(size);
        for (int i = 0; i < size; i++) {
            list.add(f.e());
        }
        return list;
    }

	
    /**
     * Create a string connecting all values in collection, separated with delimiter
     * @param objs
     * @param delimiter
     * @return
     */
	public static <A> String join(Iterable<A> objs, String delimiter) {
        if (objs == null) {
            return "";
        }
		StringBuilder sb = new StringBuilder();
		for (A a : objs) {
			sb.append(a).append(delimiter);
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - delimiter.length());
		}
		return sb.toString();
	}
	
	
	public static <A,T> List<T> yield(List<A> col, F1<A, T> f1) {
        if (col!=null) {
            return yield(col, new ArrayList<T>(col.size()), f1);
        } else {
            return null;
        }
    }
	
	/**
	 * Apply function on every elements to get new collection of returned value
	 * @param <A>
	 * @param <T>
	 * @param <C>
	 * @param inputs
	 * @param col
	 * @param f1
	 * @return
	 */
	public static <A,T,C extends Collection<T>> C yield(Iterable<A> inputs, C col, F1<A, T> f1) {
//		ArrayList<T> list = new ArrayList<T>();
		if (inputs!=null) {
            for (A a : inputs) {
                T e = f1.e(a);
				if (e != null) {
					col.add(e);
				}
            }
		}
		return col;
	}

	/**
     * Create a map based on the Object... param. Each 2 values is an entry
     * which is a pair of key then value
     * @param objects The params that will be converted to map.
     * 					Format: [key1, value1, key2, value2]
     * @return The map after converted from param objects
     */
//    @SuppressWarnings({"unchecked"})
    public static <A, B> Map<A, B> map(Object... objects) {
    	if (objects==null) {
    		return null;
    	}
        Map<A, B> map = new LinkedHashMap<A, B>(objects.length / 2);
        for (int i = 0; i < objects.length; i+=2) {
            map.put((A)objects[i], (B)objects[i + 1]);
        }
        return map;
    }
}

