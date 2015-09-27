package net.ko.converters;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class KoTimeConverter {
	public static Time valueOf(String dateStr) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.koTFormat);
	    java.util.Date ud=formatter.parse(dateStr);
	    return new Time(ud.getTime());
	}
}
