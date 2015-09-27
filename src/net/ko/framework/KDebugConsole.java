package net.ko.framework;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import net.ko.utils.KString;

public class KDebugConsole {
	private String options;
	private static KDebugConsole instance = null;
	private static DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.FRENCH);

	public static KDebugConsole getInstance() {
		if (instance == null) {
			synchronized (KDebugConsole.class) {
				instance = new KDebugConsole();
			}
		}
		return instance;
	}

	private KDebugConsole() {
		options = "";
	}

	public static void print(String message, String type, String context) {
		KDebugConsole inst = getInstance();
		if (("," + inst.options + ",").contains("," + type + ",")) {
			Calendar cal = Calendar.getInstance();
			Date d = cal.getTime();
			System.out.println(df.format(d) + " " + type + " : " + context + " -> " + message);
			Ko.klogger().log(Level.INFO, type + " : " + context + " -> " + message);
		}
	}

	public static String getOptions() {
		return getInstance().options;
	}

	public static void setOptions(String options) {
		if (options != null) {
			if (KString.isBooleanTrue(options))
				options = "SQL,POOL,MAPPING,AJAX,CACHE,FRAMEWORK,KOBJECT,TEMPLATE,REQUEST,PAGE,PARSER,DAOEngine,CONFIG,CLIENTDEBUG";
			if (KString.isBooleanFalse(options))
				options = "";
			options = options.replaceAll("\\W", ",");
			getInstance().options = options.toUpperCase();
		}
	}

}
