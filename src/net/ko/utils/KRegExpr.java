package net.ko.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ko.interfaces.IStringCommande;

public class KRegExpr {

	public static ArrayList<String> getGroups(String regExpr, String input, int position) {
		ArrayList<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile(regExpr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(input);
		while (m.find()) {
			if (m.groupCount() > position - 1) {
				result.add(m.group(position));
			}
		}
		return result;
	}

	public static String applyToGroups(String sepFirst, String sepLast, String input, IStringCommande command) {
		String result = input;
		Pattern p = Pattern.compile(Pattern.quote(sepFirst) + "(.*?)" + Pattern.quote(sepLast), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(input);
		while (m.find()) {
			if (m.groupCount() > 0) {
				String strOrigine = m.group(1);
				result = result.replace(sepFirst + strOrigine + sepLast, command.execute(strOrigine));
			}
		}
		return result;
	}

	public static String getPart(String sepFirst, String sepLast, String input) {
		return getPart(sepFirst, sepLast, input, false);
	}

	public static String replaceBetween(String sepFirst, String sepLast, String input, String replaceWith) {
		String result = input;
		result = result.replaceFirst(Pattern.quote(sepFirst) + "(.*?)" + Pattern.quote(sepLast), replaceWith);
		return result;
	}

	public static String getPart(String sepFirst, String sepLast, String input, boolean large) {
		String largeStr = "";
		if (!large)
			largeStr = "?";
		String result = input;
		Pattern p = Pattern.compile(Pattern.quote(sepFirst) + "((\\s|.)*" + largeStr + ")" + Pattern.quote(sepLast), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);
		if (m.find()) {
			if (m.groupCount() > 0) {
				result = m.group(1);
			}
		}
		return result;
	}

	public static int getCount(String sepFirst, String sepLast, String input) {
		int result = 0;
		Pattern p = Pattern.compile(Pattern.quote(sepFirst) + "((\\s|.)+?)" + Pattern.quote(sepLast), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);
		while (m.find()) {
			if (m.groupCount() > 0) {
				result++;
			}
		}
		return result;
	}
}
