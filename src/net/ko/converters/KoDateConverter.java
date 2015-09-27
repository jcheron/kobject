package net.ko.converters;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class KoDateConverter {
	public static Date valueOf(String dateStr) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.koFormat);
	    java.util.Date ud=formatter.parse(dateStr);
	    return new Date(ud.getTime());
	}
}
