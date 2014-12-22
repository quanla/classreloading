package qj.util.funct;

public class FsGenerated {

	/**
	 * Convert from a p0 to p1
	 * @return p1
	 */
	public static <A> P1<A> p1(final P0 p0) {
		return new P1<A>(){public void e(final A a) {
			p0.e();
		}};
	}
	
	/**
	 * Call to p and return fixed value
	 * @param p1 the function to call before return value
	 * @param ret the fixed value to return
	 * @return ret
	 */
	public static <A, R> F1<A, R> f1(final P1<A> p1, final R ret) {
		return new F1<A, R>(){public R e(final A a) {
			p1.e(a);
			return ret;
		}};
	}
	
	/**
	 * Call to p and return fixed value
	 * @param p2 the function to call before return value
	 * @param ret the fixed value to return
	 * @return ret
	 */
	public static <A, B, R> F2<A, B, R> f2(final P2<A, B> p2, final R ret) {
		return new F2<A, B, R>(){public R e(final A a, final B b) {
			p2.e(a, b);
			return ret;
		}};
	}
}