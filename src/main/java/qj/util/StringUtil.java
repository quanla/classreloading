package qj.util;

import qj.util.math.Range;

/**
 * Created by Quan on 22/12/2014.
 */
public class StringUtil {
	
	/**
	 * Trim the source and uppercase it's first character
	 *
	 * @param source -
	 *            The string to be malnipulated
	 * @return The result String, null if the source String is null
	 */
	public static String upperCaseFirstChar(String source) {
		if (source == null)
			return null;

		source = source.trim();

		if (source.length() == 0)
			return "";
		else
			return Character.toUpperCase(source.charAt(0)) + source.substring(1, source.length());
	}
	
	/**
	 *
	 * @return count
	 */
	public static int countHappens(char chr, CharSequence string) {
		if (string==null) {
			return 0;
		}
		int length = string.length();
		int count = 0;
		for (int i = 0; i < length; i++) {
			if (string.charAt(i) == chr)
				count++;
		}
		return count;
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static String replace(String replace, Range range, String text) {
		return text.substring(0, range.getFrom()) + replace + text.substring(range.getTo());
	}
}
