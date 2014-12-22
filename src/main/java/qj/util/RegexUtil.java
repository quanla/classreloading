package qj.util;

import qj.util.funct.F1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static F1<Object,Pattern> compileF = new F1<Object, Pattern>() {
        public Pattern e(Object obj) {
            return Pattern.compile(obj.toString());
        }
    };

	public static boolean matches(String key, String ptn) {
		return compileF.e(ptn).matcher(key).matches();		
	}
	
	
	public static Matcher matcher(String regex, String str) {
		return compileF.e(regex).matcher(str);
	}
	
	public static String replaceAll(String text,String ptn, F1<Matcher, String> f1) {
		return StringChange.apply(replaceAll(f1, compileF.e(ptn), text), text);
	}
	
	public static List<StringChange> replaceAll(
			F1<Matcher, String> f1, 
			Pattern ptn,
			String text) {
		return replaceAll(f1, ptn, text, 0, -1);
	}

	public static List<StringChange> replaceAll(
			F1<Matcher, String> f1, 
			Pattern ptn,
			String text,
			int from,
			int to) {
		Matcher matcher = ptn.matcher(text);
		
		ArrayList<StringChange> changes = new ArrayList<>();
		if (matcher.find(from)) {
			do {
				changes.add(StringChange.replace(matcher.start(), matcher.end(), f1.e(matcher)));
			} while (matcher.find() && (to==-1 || matcher.start() < to));
		}
		return changes;
	}
}
