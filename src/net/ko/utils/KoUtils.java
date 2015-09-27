package net.ko.utils;

import java.lang.reflect.Array;

import net.ko.kobject.KObject;

public class KoUtils {
	private final static String koClassPrefix = "K";

	public static String getClassName(String tableName) {
		return koClassPrefix + KString.capitalizeFirstLetter(tableName);
	}

	public static String getTableName(Class<? extends KObject> clazz) {
		String result = clazz.getSimpleName().replaceFirst("^" + koClassPrefix + "(.*)", "$1");
		result = KString.unCapitalizeFirstLetter(result);
		return result;
	}

	public static String getVariableListName(Class<? extends KObject> clazz) {
		String result = "";
		result = clazz.getSimpleName();
		result = result.substring(1).toLowerCase() + "s";
		return result;
	}

	public static String getFkFieldName(Class<? extends KObject> clazz) {
		String result = "";
		result = clazz.getSimpleName();
		result = "id" + KString.capitalizeFirstLetter(result.substring(1));
		return result;
	}

	public static String getFkFieldName(String member) {
		return "id" + KString.capitalizeFirstLetter(member);
	}

	public static Object getInstance(String completeClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object result = null;
		Class<? extends Object> clazz = Class.forName(completeClassName);
		if (clazz != null)
			result = clazz.newInstance();
		return result;
	}

	public static Object getInstance(Class<? extends Object> clazz) throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

	@SuppressWarnings("rawtypes")
	public static Object extend(Object a, int size) {
		Class cl = a.getClass();
		if (!cl.isArray())
			return null;
		int length = Array.getLength(a);
		int newLength = length + size;
		Class componentType = cl.getComponentType();
		Object newArray = Array.newInstance(componentType, newLength);
		System.arraycopy(a, 0, newArray, 0, length);
		return newArray;
	}
}
