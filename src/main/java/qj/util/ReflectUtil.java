package qj.util;

import qj.util.funct.F1;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Quan on 22/12/2014.
 */
public class ReflectUtil {
	
	public static <A> A newInstance(Class<A> cla) {
        return (A) newInstance4(cla);
	}
    public static Object newInstance4(Class clazz) {
    	
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
            Throwable cause = e.getCause();
            if (cause==null) {
            	cause = e;
            }
			throw new RuntimeException(cause);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
	
	
    public static Method getMethod(String methodName, Class clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		if (!clazz.equals(Object.class)) {
			Class superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getMethod(methodName, superclass);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
    public static Method getMethod(String methodName, Class[] paramClasses, Class<?> clazz) {
        try {
            return clazz.getMethod(methodName, paramClasses);
        } catch (NoSuchMethodException e) {
            if (!clazz.equals(Object.class)) {
				Class<?> superclass = clazz.getSuperclass();
				if (superclass != null) {
					return getMethod(methodName, paramClasses, superclass);
				}
				return null;
			} else {
				return null;
			}
        }
    }
	
	/**
	 * Invoke the method with given params
	 * @param method
	 * @param o
	 * @param params
	 * @return
	 */
    public static <T> T invoke(Method method, Object o, Object... params) {
        try {
            return (T) method.invoke(o, params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
	
	
    public static void setFieldValue(Object value, Field field, Object obj) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
	
	public static <A> A getFieldValue(String field, Object obj) {
		return getFieldValue(getField(field, obj.getClass()), obj);
	}

    public static <A> A getFieldValue(Field field, Object obj) {
        try {
            return (A) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
	
	
    public static Field getField(String name, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (Object.class.equals(superClass)) {
                return null;
            } else {
                return getField(name, superClass);
            }
        }
    }
	
	public static Method findMethod(Class clazz, F1<Method,Boolean> f1) {
		AtomicReference<Method> m = new AtomicReference<>();
		eachMethod(clazz, (Method obj) -> {
			if (f1.e(obj)) {
				m.set(obj);
				return true;
			}
			return false;
		});
		return m.get();
	}
	
	public static void eachMethod(Class clazz, F1<Method,Boolean> f1) {
		
		for (Method method : clazz.getMethods()) {
			if (f1.e(method)) {
				return;
			}
		}
		if (!clazz.equals(Object.class)) {
			Class superclass = clazz.getSuperclass();
			if (superclass != null) {
				eachMethod(superclass, f1);
			}
		}
	}
	
	/**
	 * Invoke the method with given params
	 */
	public static Object invoke(String methodName, Object o, Object... params) {
		return invoke(getMethod(methodName, o.getClass()), o, params);
	}
	
    public static void setFieldValue(Object value, String field, Object obj) {
        try {
            setFieldValue(value, obj.getClass().getDeclaredField(field), obj);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

	public static void invokeStatic(String methodName, Class<?> clazz) {
		invoke(getMethod(methodName, clazz), null, null);
	}
	
    public static <A> A getStaticFieldValue(String field, Class clazz) {
        try {
            return (A) getFieldValue(clazz.getDeclaredField(field), null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
