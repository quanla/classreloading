package qj.util.math;

import qj.util.ObjectUtil;

/**
 */
public class Range implements Comparable<Range> {
	private Integer from;
	private Integer to;

    public Range() {
    }

    public Range(Integer from, Integer to) {
        this.from = from;
        this.to = to;
        if (to != null && to < from) {
        	throw new RuntimeException("to(" + to + ") < from(" + from + ")");
        }
    }

    public Range(Long start, Long end) {
        this.from 	= start	== null ? null : start.intValue();
        this.to 	= end   == null ? null : end.intValue();
        if (to != null && to < from) {
        	throw new RuntimeException();
        }
	}

	public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String toString() {
        return from + "-" + to;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Range)) {
            return false;
        }
        Range o2 = (Range) obj;
        return ObjectUtil.equals(from, o2.from)
                && ObjectUtil.equals(to, o2.to);
    }


    public int length() {
        return to - from;
    }
    public boolean isEmpty() {
        return to.equals(from);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public int compareTo(Range o) {
        return from.compareTo(((Range)o).getFrom());
//    	return MathUtil.distance(this, o);
    }
    

	public static Range fromlength(int from, int length) {
		return new Range(from, from + length);
	}

}
