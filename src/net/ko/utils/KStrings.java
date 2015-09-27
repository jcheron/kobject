/**
 * Classe KStrings
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2012
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KStrings.java,v 1.10 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.utils
 */
package net.ko.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import net.ko.types.EscapeChar;
import net.ko.types.KStringsType;

/**
 * Classe utilitaire représentant une liste de chaînes
 * 
 * @author jcheron
 * 
 */
public class KStrings implements Iterable<String> {
	private String equals = "=";
	private String separator = ",";
	private String valwrap = "";

	public enum KGlueMode {
		KEY, VALUE, KEY_AND_VALUE, VALUE_AND_KEY
	};

	private Map<String, Object> strings;

	public Map<String, Object> getStrings() {
		return strings;
	}

	public Map<String, Object> getSortedStrings() {
		return new TreeMap<>(strings);
	}

	public Map<String, String[]> getMapArrayStrings() {
		Map<String, String[]> result = new LinkedHashMap<String, String[]>();
		for (Map.Entry<String, Object> e : strings.entrySet()) {
			result.put(e.getKey(), new String[] { e.getValue() + "" });
		}
		return result;
	}

	public KStrings() {
		super();
		strings = new LinkedHashMap<String, Object>();
	}

	public KStrings(String values, String separator) {
		this(values.split(separator));
		this.separator = separator;
	}

	public KStrings(String JSONString) {
		this(JSONString, KStringsType.kstJSON);
	}

	public KStrings(KStringsType stringType) {
		strings = new LinkedHashMap<String, Object>();
		setType(stringType);
	}

	public KStrings(String values, KStringsType stringType) {
		setType(stringType);
		switch (stringType) {
		case kstJSON:
			strings = getJSONStrings(values);
			break;
		case kstQueryString:
			strings = getStrings(values, separator, equals);
			break;
		}
	}

	private static Map<String, Object> getJSONStrings(String values) {
		String separator = ",";
		String equal = ":";
		Map<String, Object> strings = new LinkedHashMap<String, Object>();
		if (values != null && !"".equals(values)) {
			values = KString.cleanJSONString(values);
			for (EscapeChar ec : EscapeChar.values()) {
				values = protectedStringFrom(values, ec.getC(), ec.getReplacement());
			}
			// values=protectedStringFrom(values,',',"%virgule%");
			// values=protectedStringFrom(values,':',"%deuxPoints%");
			String[] strs = values.split(separator);
			if (values.contains(equal)) {
				for (String str : strs) {
					String[] s = str.split(equal);
					if (s.length >= 2) {
						str = str.replaceFirst("\\s?:\\s?", ":");
						str = str.replaceFirst("^\\s+", "");
						String k = KString.cleanJSONValue(s[0].trim());
						String v = KString.cleanJSONValue(str.replace(s[0].trim() + equal, ""));
						strings.put(k, restoreStringFrom(v));
					}
				}
			} else {
				for (int i = 0; i < strs.length; i++) {
					String v = KString.cleanJSONValue(strs[i]);
					strings.put(String.valueOf(i), restoreStringFrom(v));
				}
			}
		}
		return strings;
	}

	private static String protectedStringFrom(String values, char c, String replacement) {
		boolean isopen = false;
		String result = "";
		for (int i = 0; i < values.length(); i++) {
			if (!isopen && values.charAt(i) == '"')
				isopen = true;
			else if (isopen && values.charAt(i) == '"')
				isopen = false;
			if (isopen && values.charAt(i) == c)
				result += replacement;
			else
				result += values.charAt(i);
		}
		return result;
	}

	private static String restoreStringFrom(String values) {
		String result = values;
		for (EscapeChar ec : EscapeChar.values()) {
			result = result.replaceAll(ec.getReplacement(), ec.getC() + "");
		}
		return result;
	}

	private static Map<String, Object> getStrings(String values, String separator, String equal) {
		Map<String, Object> strings = new LinkedHashMap<String, Object>();
		if (values != null && !"".equals(values)) {
			String[] strs = values.split(separator);
			if (values.contains(equal)) {
				for (String str : strs) {
					String[] s = str.split(equal);
					if (s.length >= 2) {
						String k = s[0].trim();
						String v = str.replace(s[0].trim() + equal, "");
						strings.put(k, v);
					}
				}
			} else {
				for (int i = 0; i < strs.length; i++) {
					String v = strs[i];
					strings.put(String.valueOf(i), v);
				}
			}
		}
		return strings;
	}

	public KStrings(String values, String separator, String equal) {
		this.separator = separator;
		this.equals = equal;
		strings = getStrings(values, separator, equal);
	}

	public KStrings(String[] values) {
		this.strings = new LinkedHashMap<String, Object>();
		for (int i = 0; i < values.length; i++) {
			this.strings.put(String.valueOf(i), values[i]);
		}
	}

	public KStrings(Object[] values) {
		this.strings = new LinkedHashMap<String, Object>();
		for (int i = 0; i < values.length; i++) {
			this.strings.put(String.valueOf(i), values[i]);
		}
	}

	public KStrings(Properties properties) {
		this.strings = new LinkedHashMap<String, Object>();
		for (Map.Entry<? extends Object, ? extends Object> e : properties.entrySet()) {
			this.strings.put(e.getKey() + "", e.getValue());
		}
	}

	public KStrings(List<String> strings) {
		super();
		this.strings = new LinkedHashMap<String, Object>();
		int i = 0;
		for (String string : strings) {
			this.strings.put(String.valueOf(i), string);
			i++;
		}
	}

	public KStrings(Set<String> strings) {
		super();
		this.strings = new LinkedHashMap<String, Object>();
		int i = 0;
		for (String string : strings) {
			this.strings.put(String.valueOf(i), string);
			i++;
		}
	}

	public KStrings(Map<String, ?> strings) {
		super();
		this.strings = new LinkedHashMap<>();
		if (strings != null)
			for (Map.Entry<String, ?> e : strings.entrySet())
				this.strings.put(e.getKey(), e.getValue());
	}

	public String implode(String delim) {
		return implode(delim, strings.values().toArray());
	}

	public String implode() {
		return implode_param(separator, valwrap, equals, KGlueMode.KEY_AND_VALUE);
	}

	public String implode(KGlueMode mode) {
		return implode_param(separator, valwrap, equals, mode);
	}

	public String implode(String delim, String valwrap) {
		return implode(delim, strings.values().toArray(), valwrap);
	}

	public static String implode(String delim, Object[] args) {
		return implode(delim, args, "");
	}

	public static String implode(String delim, Object[] args, String valwrap) {
		StringBuffer sb = new StringBuffer();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (i > 0)
					sb.append(delim);
				sb.append(valwrap + args[i] + valwrap);
			}
		}

		return sb.toString();
	}

	public static String implode(String delim, List<String> args, String valwrap) {
		StringBuffer sb = new StringBuffer();
		if (args != null) {
			for (int i = 0; i < args.size(); i++) {
				if (i > 0)
					sb.append(delim);
				sb.append(valwrap + args.get(i) + valwrap);
			}
		}

		return sb.toString();
	}

	public static String implode_param(Map<String, Object> fields, String glue, String valwrap, String equals, KGlueMode mode, String keyWrap, boolean slashes) {
		return new KStrings(fields).implode_param(glue, valwrap, equals, mode, keyWrap, slashes);
	}

	public static String implode_param(Map<String, Object> fields, String glue, String valwrap, String equals, KGlueMode mode, boolean slashes) {
		return new KStrings(fields).implode_param(glue, valwrap, equals, mode, slashes);
	}

	public static String implode_param(Map<String, Object> map, String glue, String valwrap, KGlueMode mode, boolean slashes) {
		return new KStrings(map).implode_param(glue, valwrap, "=", mode, slashes);
	}

	public static String implodeAndReplaceValues(Map<String, Object> map, String replaceValue, String glue, String valwrap, String equals, String keyWrap) {
		return new KStrings(map).implodeAndReplaceValues(replaceValue, glue, valwrap, equals, keyWrap);
	}

	public String implode_param(String glue, String valwrap, String equals, KGlueMode mode) {
		return implode_param(glue, valwrap, equals, mode, false);
	}

	public String implode_param(String glue, String valwrap, KGlueMode mode, boolean slashes) {
		return implode_param(glue, valwrap, "=", mode, slashes);
	}

	public String showWithMask(String glue, String mask) {
		String[] ret = new String[strings.size()];
		int i = 0;
		for (Map.Entry<String, Object> str : strings.entrySet()) {
			ret[i] = mask.replaceAll("%Key%", KString.capitalizeFirstLetter(str.getKey()) + "").replaceAll("%Value%", KString.capitalizeFirstLetter(str.getValue() + ""));
			ret[i] = ret[i].replaceAll("(?i)%key%", str.getKey() + "").replaceAll("(?i)%value%", str.getValue() + "");
			i++;
		}
		return implode(glue, ret);
	}

	private static String getValue(String str, boolean slashes) {
		String result = str;
		if (slashes)
			result = KString.addSlashes(str);
		return result;
	}

	public static String implode(String[] values, String key, String glue, String valwrap, String equals, KGlueMode mode, boolean slashes) {
		return implode(values, key, glue, valwrap, equals, mode, "", slashes);
	}

	public static String implode(Object[] values, String key, String glue, String valwrap, String equals, KGlueMode mode, String keyWrap, boolean slashes) {
		String[] ret = new String[values.length];
		int i;
		switch (mode) {
		case KEY:
			for (i = 0; i < values.length; i++) {
				ret[i] = valwrap + key + valwrap;
			}
			break;

		case VALUE:
			for (i = 0; i < values.length; i++) {
				if (values[i] == null)
					ret[i] = "NULL";
				else
					ret[i] = valwrap + getValue(values[i] + "", slashes) + valwrap;
			}
			break;
		case VALUE_AND_KEY:
			for (i = 0; i < values.length; i++) {
				ret[i] = getValue(values[i] + "", slashes) + equals + valwrap + key + valwrap;
			}
			break;
		default:
		case KEY_AND_VALUE:
			for (i = 0; i < values.length; i++) {
				if (values[i] == null)
					ret[i] = keyWrap + key + keyWrap + equals + "NULL";
				else
					ret[i] = keyWrap + key + keyWrap + equals + valwrap + getValue(values[i] + "", slashes) + valwrap;
			}
			break;

		}
		return implode(glue, ret);
	}

	public String implode_param(String glue, String valwrap, String equals, KGlueMode mode, boolean slashes) {
		return implode_param(glue, valwrap, equals, mode, "", slashes);
	}

	public String implodeAndReplaceValues(String replaceValue, String glue, String valwrap, String equals, String keyWrap) {
		String[] ret = new String[strings.size()];
		int i = 0;
		for (Map.Entry<String, Object> str : strings.entrySet()) {
			ret[i] = keyWrap + str.getKey() + keyWrap + equals + valwrap + replaceValue + valwrap;
			i++;
		}
		return implode(glue, ret);
	}

	public String implode_param(String glue, String valwrap, String equals, KGlueMode mode, String keyWrap, boolean slashes) {
		String[] ret = new String[strings.size()];
		int i;
		switch (mode) {
		case KEY:
			for (i = 0; i < strings.size(); i++) {
				ret[i] = valwrap + strings.keySet().toArray()[i] + valwrap;
			}
			break;

		case VALUE:
			i = 0;
			for (Map.Entry<String, Object> str : strings.entrySet()) {
				if (str.getValue() == null)
					ret[i] = "NULL";
				else
					ret[i] = valwrap + getValue(str.getValue() + "", slashes) + valwrap;
				i++;
			}
			break;
		case VALUE_AND_KEY:
			i = 0;
			for (Map.Entry<String, Object> str : strings.entrySet()) {
				ret[i] = getValue(str.getValue() + "", slashes) + equals + valwrap + str.getKey() + valwrap;
				i++;
			}
			break;
		default:
		case KEY_AND_VALUE:
			i = 0;
			for (Map.Entry<String, Object> str : strings.entrySet()) {
				if (str.getValue() == null)
					ret[i] = str.getKey() + equals + "NULL";
				else
					ret[i] = keyWrap + str.getKey() + keyWrap + equals + valwrap + getValue(str.getValue() + "", slashes) + valwrap;
				i++;
			}
			break;

		}

		return implode(glue, ret);
	}

	public boolean contains(String regEx) {
		boolean trouve = false;
		int i = 0;
		while (i < strings.size() && !trouve) {
			String str = strings.get(String.valueOf(i)).toString();
			trouve = regEx.matches(str);
			i++;
		}
		return trouve;
	}

	public ArrayList<String> toArrayList() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Map.Entry<String, Object> str : strings.entrySet()) {
			ret.add((String) str.getValue());
		}
		return ret;
	}

	public String get(int index) {
		String result = "";
		ArrayList<String> al = toArrayList();
		if (al.size() < index)
			result = al.get(index);
		return result;
	}

	public String getFirstKey() {
		return strings.keySet().iterator().next();
	}

	public Object get(String key) {
		return strings.get(key);
	}

	public void put(String key, Object value) {
		strings.put(key, value);
	}

	public boolean containsKey(String key) {
		return strings.containsKey(key);
	}

	@Override
	public Iterator<String> iterator() {
		Set<String> st = strings.keySet();
		return st.iterator();
	}

	public static boolean inArray(String[] haystack, String needle) {
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i] != null)
				if (haystack[i].equals(needle)) {
					return true;
				}
		}
		return false;
	}

	public String toJSON() {
		String jsonStr = "";
		jsonStr = implode_param(",", "\"", ":", KGlueMode.KEY_AND_VALUE, "\"", false);
		return "{" + jsonStr + "}";
	}

	public void setType(KStringsType stringType) {
		switch (stringType) {
		case kstJSON:
			separator = ",";
			equals = ":";
			break;
		case kstQueryString:
			separator = "&";
			equals = "=";
			break;
		}
	}
}
