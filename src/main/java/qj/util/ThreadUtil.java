package qj.util;

import qj.util.funct.F0;
import qj.util.funct.Fs;
import qj.util.funct.P0;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	public static void runStrong(final P0 p0) {
		executorService.execute(Fs.runnable(p0));
	}
	
	
	public static <A> ThreadLocalCache<A> threadLocalCache(final F0<A> f) {
		
		final ThreadLocal<A> threadLocal = new ThreadLocal<A>();
		
		ThreadLocalCache<A> ret = new ThreadLocalCache<A>();
		
		ret.cacheF = new F0<A>() {public A e() {
			A a = threadLocal.get();
			if (a==null) {
				a = f.e();
				threadLocal.set(a);
			}
			return a;
		}};
		ret.removeF = new F0<A>() {public A e() {
			A a = threadLocal.get();
			threadLocal.set(null);
			return a;
		}};
		
		return ret;
	}
	
	public static class ThreadLocalCache <A> {
		public F0<A> cacheF;
		public F0<A> removeF;
	}
	
	/**
	 * Sleep and wake on InterruptedException
	 * @param timeToSleep in milliseconds
	 */
	public static void sleep(long timeToSleep) {
		if (timeToSleep <=0)
			return;
		try {
			Thread.sleep(timeToSleep);
		} catch (InterruptedException e) {
		}
	}
	
}
