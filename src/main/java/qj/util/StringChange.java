package qj.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import qj.util.math.Range;

public abstract class StringChange implements Comparable<StringChange> {
	int priority = 0;
	public static StringChange replace(int start, int end, String replace) {
		return new ReplaceStringChange(start, end, replace);
	}
	public static StringChange replace(Range range, String replace) {
		return new ReplaceStringChange(range.getFrom(), range.getTo(), replace);
	}
	
	public abstract int pos();

	public abstract void apply(StringBuilder sb);

	public int compareTo(StringChange o) {
		int ret = pos() - o.pos();
		if (ret==0
//				&& o instanceof InsertStringChange
		) {
			if (this instanceof ReplaceStringChange) {
				return 1;
			} else {
				return -this.priority + o.priority;
			}
			
		}
		return ret;
	}

	public static class ReplaceStringChange extends StringChange {
		private final int start;
		private final int end;
		private final String replace;

		public ReplaceStringChange(int start, int end, String replace) {
			this.start = start;
			this.end = end;
			this.replace = replace;
		}

		public int pos() {
			return start;
		}

		public void apply(StringBuilder sb) {
			sb.replace(start, end, replace);
		}
	}
	public static class InsertStringChange extends StringChange {
		private final int pos;
		private final String value;

		public InsertStringChange(int pos, String value) {
			this.pos = pos;
			this.value = value;
		}
		public InsertStringChange(int pos, String value, int priority) {
			this.pos = pos;
			this.value = value;
			this.priority = priority;
		}

		public int pos() {
			return pos;
		}

		public void apply(StringBuilder sb) {
			sb.insert(pos, value);
		}
	}

	/**
	 * @param replaces
	 * @param text No need to sort first
	 * @return
	 */
	public static String apply(Collection<StringChange> replaces, String text) {
		ArrayList<StringChange> list = new ArrayList<StringChange>(replaces);
		Collections.sort(list);
		StringBuilder sb = new StringBuilder(text);
		for (int i = list.size() - 1; i > -1; i--) {
			StringChange change = list.get(i);
			change.apply(sb);
		}
		return sb.toString();
	}

	public static StringChange insert(String string, int pos, int priority) {
		return new InsertStringChange(pos, string, priority);
	}
	public static StringChange insert(String string, int pos) {
		return new InsertStringChange(pos, string);
	}

	public static StringChange delete(final Range selection) {
		return new StringChange() {
			public int pos() {
				return selection.getFrom();
			}

			@Override
			public void apply(StringBuilder sb) {
				sb.replace(selection.getFrom(), selection.getTo(), "");
			}
		};
	}
	public static LinkedList<StringChange> replaceAll(String replaceFrom, String replaceTo,
			String to) {
		LinkedList<StringChange> ret = new LinkedList<StringChange>();
		for (int indexOf=0; (indexOf = to.indexOf(replaceFrom, indexOf)) > -1;) {
			ret.add(replace(indexOf, indexOf + replaceFrom.length(), replaceTo));
			indexOf+=replaceFrom.length();
		}
		return ret;
	}
}
