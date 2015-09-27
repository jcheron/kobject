package net.ko.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.ko.converters.DateFormatter;

public class Converter {
	@SuppressWarnings("rawtypes")
	private static Map<Class, Class> primitiveMap = new HashMap<Class, Class>();
	static {
		primitiveMap.put(boolean.class, Boolean.class);
		primitiveMap.put(byte.class, Byte.class);
		primitiveMap.put(char.class, Character.class);
		primitiveMap.put(short.class, Short.class);
		primitiveMap.put(int.class, Integer.class);
		primitiveMap.put(long.class, Long.class);
		primitiveMap.put(float.class, Float.class);
		primitiveMap.put(double.class, Double.class);
	}

	public static Class<?> getWrapperClass(Class<?> primitiveType) {
		if (primitiveType.isPrimitive())
			return primitiveMap.get(primitiveType);
		else
			return primitiveType;
	}

	public static Object convert(String value, Class<?> destClass) {
		if ((value == null) || "".equals(value)) {
			return value;
		}

		if (destClass.isPrimitive()) {
			destClass = primitiveMap.get(destClass);
		}

		try {
			Method m = destClass.getMethod("valueOf", String.class);
			int mods = m.getModifiers();
			if (Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
				m.setAccessible(true);
				return m.invoke(null, value);
			}
		} catch (NoSuchMethodException e) {
			if (destClass == Character.class) {
				return Character.valueOf(value.charAt(0));
			}
			if (destClass == java.sql.Time.class) {
				return DateFormatter.getKoTime(value);
			}
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		return value;
	}

	public static boolean isTypeCompatible(Class<?> toType, Class<?> fromType, boolean strict) {
		boolean result = false;
		if (toType == null && fromType != null)
			result = false;
		if (toType != null && fromType == null)
			result = false;
		if (toType == null && fromType == null)
			result = true;

		if (strict)
			result = toType.equals(fromType);
		else {
			if (toType.isPrimitive())
				toType = primitiveMap.get(toType);
			if (fromType.isPrimitive())
				fromType = primitiveMap.get(fromType);
			result = toType.isAssignableFrom(fromType);
		}
		return result;
	}

}