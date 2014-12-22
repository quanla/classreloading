package qj.util.funct;


import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The utility that employ idea of functional programming
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class Fs extends FsGenerated {
	
    public static Runnable runnable(final P0 action) {
        return new Runnable() {
            public void run() {
                action.e();
            }
        };
    }
	
	/**
	 * Just store the object to collection
	 * @param col
	 * @return
	 */
    public static <A> P1<A> store(final Collection<A> col) {
        return new P1<A>() {public void e(A a) {
            col.add(a);
        }};
    }
    
	public static <A> P1<A> setter(
			final AtomicReference<A> ref) {
		return new P1<A>() {public void e(A obj) {
			ref.set(obj);
		}};
	}
	
	public static <A> void invokeAll(final Collection<P1<A>> col, A a) {
		for (P1<A> p1 : col) {
			p1.e(a);
		}
	}
}
