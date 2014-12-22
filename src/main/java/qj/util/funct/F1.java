package qj.util.funct;

/**
 * Represent a function that accept one parameter and return value
 * @param <A> The only parameter
 * @param <T> The return value
 */
public interface F1<A, T> {
    /**
     * Evaluate or execute the function
     * @param obj The parameter
     * @return Result of execution
     */
	T e(A obj);
}
