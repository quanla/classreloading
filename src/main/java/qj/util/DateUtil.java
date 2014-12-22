package qj.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Quan on 22/12/2014.
 */
public class DateUtil {
	
    /**
     * Format the time to dataFormat and timeZone
     * @param time The java Date object to be formatted
     * @param dateFormat The string format to be use to do conversion
     * @param timeZone The timezone for the dateFormat
     * @return The String result of the formatted time.
     */
    public static String format(Date time, String dateFormat, TimeZone timeZone) {
    	SimpleDateFormat df = new SimpleDateFormat(dateFormat);
    	if (timeZone!=null) {
    		df.setTimeZone(timeZone);
    	}
		String date = df.format(time);
    	
		return date;
    }

}
