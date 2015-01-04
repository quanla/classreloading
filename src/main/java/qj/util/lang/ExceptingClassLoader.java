package qj.util.lang;

import qj.util.funct.F1;

/**
 * This class loader will not load certain classes, instead delegate to parent
 * class loader to do the job
 */
@SuppressWarnings("UnusedDeclaration")
public class ExceptingClassLoader extends DynamicClassLoader {

	private F1<String, Boolean> except;

	public ExceptingClassLoader(F1<String, Boolean> except, String... paths) {
		super(paths);
		this.except = except;
	}

	@Override
	protected byte[] loadNewClass(String name) {
		if (except.e(name)) {
            return null;
		}

		return super.loadNewClass(name);
	}
}
