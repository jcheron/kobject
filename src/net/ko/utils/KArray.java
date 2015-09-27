package net.ko.utils;

import java.util.Arrays;

public class KArray {
	public static boolean in_array(Object[] haystack, String needle) {
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i] != null)
				if (haystack[i].toString().equals(needle)) {
					return true;
				}
		}
		return false;
	}

	public static Object[] arrayOfSameValue(Object value, int size) {
		Object[] s = new Object[size];
		Arrays.fill(s, value);
		return s;
	}
}
