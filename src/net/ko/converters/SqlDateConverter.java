package net.ko.converters;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SqlDateConverter {
	public static Date valueOf(String dateStr) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.sqlFormat);
		return new Date(formatter.parse(dateStr).getTime());
	}
}
