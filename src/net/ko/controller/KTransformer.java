package net.ko.controller;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.inheritance.KReflectObject;
import net.ko.utils.KHash;
import net.ko.utils.KString;

/**
 * Méthodes utilitaires pour modifier la valeur d'un champ avant son insertion
 * dans la base de données<br>
 * A spécifier sur une balise <b>member</b> du fichier <b>kox.xml</b>
 * 
 * @author jc
 * 
 */
public class KTransformer implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	public KTransformer() {
	}

	public static String xssClean(String value) {
		String result = value;
		if (value != null)
			result = KString.xssClean(result);
		return result;
	}

	public static String htmlSpecialChars(String value) {
		String result = value;
		if (value != null)
			result = KString.htmlSpecialChars(result);
		return result;
	}

	public static String trim(String value) {
		String result = value;
		if (value != null)
			result = result.trim();
		return result;
	}

	public static String md5(String value) {
		String result = value;
		if (value != null)
			result = KHash.getMD5(result);
		return result;
	}

	public static String sha256(String value) {
		String result = value;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();
			for (int i : hash) {
				hexString.append(String.format("%02x", i));
			}
			result = new String(hexString);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "hashage impossible de " + value + " en SHA-256", e);
		}
		return result;
	}

	public static String toLower(String value) {
		String result = value;
		if (value != null)
			result = result.toLowerCase();
		return result;
	}

	public static String toUpper(String value) {
		String result = value;
		if (value != null)
			result = result.toUpperCase();
		return result;
	}

	public static String firstUpper(String value) {
		String result = value;
		if (value != null)
			result = KString.capitalizeFirstLetter(result);
		return result;
	}

	public static String firstWordUpper(String value) {
		String result = value;
		if (value != null) {
			String[] parts = value.split("\\W");
			for (String part : parts) {
				result = result.replace(part, KString.capitalizeFirstLetter(part));
			}
		}
		return result;
	}

	public static String onlyFirstUpper(String value) {
		String result = value;
		if (value != null)
			result = KString.capitalizeOnlyFirstLetter(value);
		return result;
	}

	public static String onlyFirstWordUpper(String value) {
		String result = value;
		if (value != null) {
			String[] parts = value.split("\\W");
			for (String part : parts) {
				result = result.replace(part, KString.capitalizeOnlyFirstLetter(part));
			}
		}
		return result;
	}

	public static String noBlank(String value) {
		return KString.deleteBlank(value);
	}

	public static String noSpaceChar(String value) {
		return KString.deleteSpaceChar(value);
	}

	public static String noAccent(String value) {
		return KString.noAccent(value);
	}

	public static String pluriel(String value) {
		String result = value;
		if (value != null)
			result = KString.pluriel(result);
		return result;
	}

	public static String toBoolean(String value) {
		String result = value;
		if (value != null)
			result = KString.isBooleanTrue(result) + "";
		return result;
	}

	public static String toInt(String value) {
		String result = value;
		if (value != null) {
			try {
				result = Integer.valueOf(result) + "";
			} catch (Exception e) {
				result = "0";
			}
		}
		return result;
	}

	public static String toCamelCase(String value) {
		String result = value;
		if (value != null) {
			String[] parts = value.split("[^a-zA-Z0-9]");
			result = "";
			for (String part : parts) {
				result += KString.capitalizeFirstLetter(part);
			}
		}
		return result;
	}

	public static String encodeURIComponent(String value) {
		String result = value;
		if (value != null)
			result = KString.encodeURIComponent(value);
		return result;
	}

	public static String decodeURIComponent(String value) {
		String result = value;
		if (value != null)
			result = KString.decodeURIComponent(value);
		return result;
	}

	public String parse(String transform, String value) {
		String result = value;
		if (transform != null && !"".equals(transform)) {
			String methods[] = transform.split("\\|");
			for (String method : methods) {
				try {
					result = KReflectObject.kinvoke(method, this.getClass(), new String[] { result }) + "";
				} catch (Exception e) {
					Ko.klogger().log(Level.SEVERE, "Impossible d'appeler le transformer " + method, e);
				}
			}
		}
		return result;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
