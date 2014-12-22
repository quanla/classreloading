package qj.tool.web;

import java.lang.reflect.Field;
import java.util.LinkedList;

import qj.util.ReflectUtil;
import qj.util.funct.F0;
import qj.util.funct.Fs;
import qj.util.funct.P0;
import qj.util.funct.P1;
import qj.util.lang.DynamicClassLoader;

@SuppressWarnings("UnusedDeclaration")
public class ReloadingContext {

	protected String contextClassName;
	protected Object contextO;
	F0<ClassLoader> classLoaderF;
	LinkedList<P1<Object>> afterCreateContext = new LinkedList<>();
	LinkedList<P1<Object>> beforeCloseContext = new LinkedList<>();

	@SuppressWarnings("UnusedDeclaration")
	public ReloadingContext(String contextClass, final String... classpaths) {
		this.contextClassName = contextClass;
		classLoaderF = () -> new DynamicClassLoader(classpaths);
	}

	public ReloadingContext(String contextClass, F0<ClassLoader> classLoaderF) {
		this.contextClassName = contextClass;
		this.classLoaderF = classLoaderF;
	}

	public void reload() {
		close();
		contextO = createContextObj();
		Fs.invokeAll(afterCreateContext, contextO);
	}

	public void close() {
		if (contextO!= null) {
			Fs.invokeAll(beforeCloseContext, contextO);
			contextO = null;
		}
	}

	private Object createContextObj() {
		try {
			Class<?> contextClass = classLoaderF.e().loadClass(contextClassName);
			return ReflectUtil.newInstance(contextClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void initWith(final String methodName, Object... params) {
		afterCreateContext.add(obj -> {
			ReflectUtil.invoke(methodName, obj, params);
		});
	}
	public void beforeClose(final String methodName, Object... params) {
		beforeCloseContext.add(obj -> {
			ReflectUtil.invoke(methodName, obj, params);
		});
	}
	@SuppressWarnings("UnusedDeclaration")
	public void setField(final String fieldName, Object value) {
		afterCreateContext.add(obj -> ReflectUtil.setFieldValue(value, fieldName, obj));
	}

	public <A> A get(String fieldName) {
		if (contextO == null) {
			reload();
		}
		Field field = ReflectUtil.getField(fieldName, contextO.getClass());
		return ReflectUtil.getFieldValue(field, contextO);
	}

	public void initWith(P0 afterCreateContext) {
		this.afterCreateContext.add(Fs.p1(afterCreateContext));
	}
	
	public void beforeClose(P0 beforeClose) {
		this.beforeCloseContext.add(Fs.p1(beforeClose));
	}
}
