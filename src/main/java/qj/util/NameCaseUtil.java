package qj.util;

import java.util.regex.Matcher;

import qj.util.funct.F1;

public class NameCaseUtil {
	public static String camelToHyphen(String name) {
		return RegexUtil.replaceAll(name, "[A-Z]|[0-9]+", new F1<Matcher, String>() {public String e(Matcher m) {
			return (m.start() == 0 ? "" : "_") + m.group().toLowerCase();
		}});
	}

}
