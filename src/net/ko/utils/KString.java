package net.ko.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class KString {
	private final static String NON_THIN = "[^iIl1\\.,']";

	private static int textWidth(String str) {
		return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public static String ellipsize(String text, int max) {

		if (textWidth(text) <= max)
			return text;
		int end = text.lastIndexOf(' ', max - 3);

		if (end == -1)
			return text.substring(0, max - 3) + "...";
		int newEnd = end;
		do {
			end = newEnd;
			newEnd = text.indexOf(' ', end + 1);
			if (newEnd == -1)
				newEnd = text.length();

		} while (textWidth(text.substring(0, newEnd) + "...") < max);

		return text.substring(0, end) + "...";
	}

	public static String capitalizeFirstLetter(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() == 0) {
			return value;
		}
		StringBuilder result = new StringBuilder(value);
		result.replace(0, 1, result.substring(0, 1).toUpperCase());
		return result.toString();
	}

	public static String capitalizeOnlyFirstLetter(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() == 0) {
			return value;
		}
		StringBuilder result = new StringBuilder(value.toLowerCase());
		result.replace(0, 1, result.substring(0, 1).toUpperCase());
		return result.toString();
	}

	public static String unCapitalizeFirstLetter(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() == 0) {
			return value;
		}
		StringBuilder result = new StringBuilder(value);
		result.replace(0, 1, result.substring(0, 1).toLowerCase());
		return result.toString();
	}

	public static boolean isBoolean(String value) {
		return isBooleanTrue(value) || isBooleanFalse(value);
	}

	public static boolean isBooleanTrue(String value) {
		boolean result = false;
		if (value != null) {
			result = value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on");
		}
		return result;
	}

	public static int getBooleanIntValue(String value) {
		int result = 0;
		if (value != null) {
			if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true"))
				result = 1;
		}
		return result;
	}

	public static boolean isBooleanFalse(String value) {
		boolean result = false;
		if (value != null) {
			result = value.equalsIgnoreCase("0") || value.equalsIgnoreCase("false");
		}
		return result;
	}

	public static Object convert(String str) {
		Object result = str;
		try {
			result = Integer.parseInt(str);
		} catch (NumberFormatException e1) {
			try {
				result = Float.parseFloat(str);
			} catch (NumberFormatException e2) {
				try {
					result = Date.valueOf(str);
				} catch (Exception e3) {
				}
			}
		}
		return result;
	}

	public static String addSlashes(String s) {
		if (s != null) {
			s = s.replaceAll("\\\\", "\\\\\\\\");
			s = s.replaceAll("\\n", "\\\\n");
			s = s.replaceAll("\\r", "\\\\r");
			s = s.replaceAll("\\00", "\\\\0");
			s = s.replaceAll("'", "\\\\'");
		}
		return s;
	}

	public static String cleanHTMLAttribute(String s) {
		if (s != null) {
			s = s.replaceAll("\\W", "_");
		}
		return s;
	}

	public static String htmlSpecialChars(String s) {
		if (s != null) {
			s = s.replaceAll("&", "&amp;");
			s = s.replaceAll("'", "&apos;");
			s = s.replaceAll("\"", "&quot;");
			s = s.replaceAll("<", "&lt;");
			s = s.replaceAll(">", "&gt;");
			s = s.replaceAll("é", "&eacute;");
			// TODO autres caractères
		}
		return s;
	}

	public static String xssClean(String s) {
		if (s != null) {
			s = s.replaceAll("&", "");
			s = s.replaceAll("'", "");
			s = s.replaceAll("\"", "");
			s = s.replaceAll("<", "");
			s = s.replaceAll(">", "");
			s = s.replaceAll("\"", "");
			s = s.replaceAll("--", "");
		}
		return s;
	}

	public static String jsQuote(String s) {
		if (s != null) {
			s = s.replaceAll("'", "\\\\'");
			s = s.replaceAll("\"", "\\\"");
		}
		return s;
	}

	public static String HtmlQuote(String s) {
		if (s != null) {
			// s = s.replaceAll("'", "\\\\'");
			s = s.replaceAll("\"", "\\\\\"");
			s = s.replaceAll("(\\r|\\n)", "");
		}
		return s;
	}

	public static String removeCRLF(String s) {
		return s.replaceAll("(\\r|\\n)", "");
	}

	public static Integer[] toArrayOfInt(String value, String separator) {
		String[] values = value.split(separator);
		Integer[] result = new Integer[values.length];
		for (int i = 0; i < values.length; i++) {
			try {
				result[i] = Integer.valueOf(values[i]);
			} catch (Exception e) {
				result = null;
			}
		}
		return result;
	}

	public static String cleanStringArray(String value, String separator) {
		String result = value.trim();
		result = result.replaceAll("^" + separator + "|" + separator + "$", "");
		result = result.replaceAll(separator + separator, separator);
		return result;
	}

	public static String cleanJSONValue(String value) {
		value = value.replaceAll("^\\s*|\\s*$", "");
		value = value.replaceAll("^\"(.*?)\"$", "$1");
		value = value.replaceAll("^\'(.*?)\'$", "$1");
		return value;
	}

	public static String cleanJSONString(String value) {
		return value.replaceAll("^\\{|\\}$", "");
	}

	public static String getBefore(String input, String value) {
		String result = input;
		int pos = input.indexOf(value);
		if (pos != -1)
			result = input.substring(0, pos);
		return result;
	}

	public static String getAfter(String input, String value) {
		String result = input;
		int pos = input.indexOf(value);
		if (pos != -1)
			result = input.substring(pos + 1);
		return result;
	}

	public static String getLastAfter(String input, String value) {
		String result = input;
		int pos = input.lastIndexOf(value);
		if (pos != -1)
			result = input.substring(pos + 1);
		return result;
	}

	public static boolean isNull(String s) {
		return (s == null || "".equals(s));
	}

	public static boolean isNotNull(String s) {
		return (s != null && !"".equals(s));
	}

	public static boolean isNumeric(String s) {
		return (s != null && s.matches("[-+]?\\d*\\.?\\d+"));
	}

	public static String getPart(String sepFirst, String sepLast, String input) {
		String result = input;
		int start = input.indexOf(sepFirst);
		if (start != -1) {
			start += sepFirst.length();
			result = result.substring(start);
			int end = result.lastIndexOf(sepLast);
			if (end != -1)
				result = result.substring(0, end);
		}
		return result;
	}

	public static String getFirstPart(String sepFirst, String sepLast, String input) {
		String result = input;
		int start = input.indexOf(sepFirst);
		if (start != -1) {
			start += sepFirst.length();
			result = result.substring(start);
			int end = result.indexOf(sepLast);
			if (end != -1)
				result = result.substring(0, end);
		}
		return result;
	}

	public static String pluriel(String word) {
		String result = word;
		if (endsWith(result, new String[] { "eu", "au", "eau" }))
			result += "x";
		else if (endsWith(result, new String[] { "bijou", "caillou", "chou", "genou", "hibou", "joujou", "pou" }))
			result += "x";
		else if (endsWith(result, new String[] { "bal", "carnaval", "chacal", "festival", "récital", "régal" }))
			result += "s";
		else if (endsWith(result, new String[] { "al" }))
			result = result.replace("al", "aux");
		else if (endsWith(result, new String[] { "aspirail", "bail", "corail", "émail", "fermail", "gemmail", "soupirail", "travail", "vantail", "ventail", "vitrail" }))
			result = result.replace("ail", "aux");
		else if (result.endsWith("ail"))
			result += "s";
		else if (!endsWith(result, new String[] { "s", "x", "z" }))
			result = result + "s";
		return result;
	}

	public static String pluriel(int cardinality, String words) {
		String result = "";
		if (cardinality == 0)
			result = "Aucun(e) " + words;
		else if (cardinality == 1)
			result = "1 " + words;
		else {
			String[] ws = words.split(" ");
			result = cardinality + " ";
			for (String w : ws) {
				result += " " + pluriel(w);
			}
		}
		return result;
	}

	public static String noAccent(String word) {
		return Normalizer.normalize(word, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
	}

	public static boolean endsWith(String word, String[] terms) {
		boolean result = false;
		for (int i = 0; i < terms.length; i++) {
			if (noAccent(word).endsWith(noAccent(terms[i]))) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static String addLast(String word, String sequence) {
		String result = word;
		for (int i = sequence.length() - 1; i >= 0; i--) {
			result = result.replaceFirst(Pattern.quote(sequence.substring(i, i)) + "$", "");
		}
		if (!result.endsWith(sequence))
			result += sequence;
		return result;
	}

	public static String repeat(String s, int n) {
		return new String(new char[n]).replace("\0", s);
	}

	public static String urlEncode(String s) {
		String result = s;
		try {
			result = URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = s;
		}
		return result;
	}

	public static String urlDecode(String s) {
		String result = s;
		try {
			result = URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = s;
		}
		return result;
	}

	public static String encodeURIComponent(String s)
	{
		String result = s;
		if (s != null) {
			try {
				result = URLEncoder.encode(s, "UTF-8")
						.replaceAll("\\+", "%20")
						.replaceAll("\\%21", "!")
						.replaceAll("\\%27", "'")
						.replaceAll("\\%28", "(")
						.replaceAll("\\%29", ")")
						.replaceAll("\\%7E", "~");
			} catch (UnsupportedEncodingException e) {
				result = s;
			}
		}

		return result;
	}

	public static String showWithMask(String mask, String... params) {
		String result = mask;
		for (int i = 0; i < params.length; i++) {
			result = result.replace("%" + (i + 1) + "%", params[i]);
		}
		result = result.replaceAll("%[0-9]%", "");
		return result;
	}

	public static String decodeURIComponent(String s) {
		if (s == null) {
			return null;
		}
		String result = null;

		try {
			result = URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = s;
		}

		return result;
	}

	public static String deleteBlank(String s) {
		String result = s;
		if (s != null) {
			s = s.replaceAll("\\s", "");
		}
		return result;
	}

	public static String deleteSpaceChar(String s) {
		String result = s;
		if (s != null) {
			s = s.replaceAll(" ", "");
		}
		return result;
	}

	public static int countOfChar(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}
}
