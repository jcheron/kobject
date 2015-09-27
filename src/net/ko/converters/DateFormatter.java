package net.ko.converters;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.ko.utils.Converter;

public class DateFormatter {
	public static String sqlFormat = "yyyy-MM-dd";
	public static String sqlTFormat = "HH:mm:ss";
	public static String sqlDateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String koFormat = "dd/MM/yyyy";
	public static String koTFormat = "HH:mm";
	public static String koDTFormat = "dd/MM/yyyy HH:mm";

	public static Date getSqlDate(String dateStr) {
		Date result = null;
		try {
			result = (Date) Converter.convert(dateStr, Class.forName("net.ko.converters.SqlDateConverter"));
		} catch (ClassNotFoundException e) {
		}
		return result;
	}

	public static Date getSqlDateTime(String dateStr) {
		Date result = null;
		try {
			result = (Date) Converter.convert(dateStr, Class.forName("net.ko.converters.SqlDateTimeConverter"));
		} catch (ClassNotFoundException e) {
		}
		return result;
	}

	public static Date getKoDate(String dateStr) {
		Date result = null;
		try {
			result = (Date) Converter.convert(dateStr, Class.forName("net.ko.converters.KoDateConverter"));
		} catch (ClassNotFoundException e) {
		}
		return result;
	}

	public static Time getKoTime(String timeStr) {
		Time result = null;
		try {
			result = (Time) Converter.convert(timeStr, Class.forName("net.ko.converters.KoTimeConverter"));
		} catch (ClassNotFoundException e) {
		}
		return result;
	}

	public static Date getKoDateTime(String dateStr) {
		Date result = null;
		try {
			result = (Date) Converter.convert(dateStr, Class.forName("net.ko.converters.KoDateTimeConverter"));
		} catch (ClassNotFoundException e) {
		}
		return result;
	}

	public static String toSqlDate(Date date) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.sqlFormat);
		return formatter.format(date);
	}

	public static String toSqlTime(Time time) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.sqlTFormat);
		return formatter.format(time);
	}

	public static String toSqlDate(java.util.Date date) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.sqlFormat);
		return formatter.format(date);
	}

	public static String toKoDate(Date date) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.koFormat);
		return formatter.format(date);
	}

	public static String toKoDate(java.util.Date date) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.koFormat);
		return formatter.format(date);
	}

	public static String toKoTime(Time time) {
		DateFormat formatter = new SimpleDateFormat(DateFormatter.koTFormat);
		return formatter.format(time);
	}

	public static String sqlToKoDate(String dateStr) {
		return toKoDate(getSqlDate(dateStr));
	}

	public static String koToSqlDate(String dateStr) {
		return toSqlDate(getKoDate(dateStr));
	}
}
