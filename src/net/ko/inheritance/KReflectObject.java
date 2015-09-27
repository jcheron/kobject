package net.ko.inheritance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.ko.framework.KDebugConsole;
import net.ko.utils.Converter;
import net.ko.utils.KString;

@SuppressWarnings({ "rawtypes" })
public class KReflectObject {
	public static Object kinvoke(Method method, Object o, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class[] types = method.getParameterTypes();
		Object[] params = null;
		if (paramsValues != null && paramsValues.length != 0) {
			params = new Object[paramsValues.length];
			for (int i = 0; i < types.length; i++) {
				Object param = paramsValues[i];
				try {
					param = (Object) Converter.convert((String) paramsValues[i], types[i]);
				} catch (Exception e) {
				}
				params[i] = param;
			}
		}
		method.setAccessible(true);
		return method.invoke(o, params);
	}

	public static Object kinvoke(String methodName, Object o, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (o.getClass().equals(Class.class))
			return kinvoke(methodName, o, (Class) o, paramsValues);
		else
			return kinvoke(methodName, o, o.getClass(), paramsValues);
	}

	public static Object kinvokeForSetter(String methodName, Object o, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (o.getClass().equals(Class.class))
			return kinvokeForSetter(methodName, o, (Class) o, paramsValues);
		else
			return kinvokeForSetter(methodName, o, o.getClass(), paramsValues);
	}

	public static Object kinvokeForSetter(String methodName, Object o, Class clazz, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		boolean find = false;
		Object ret = new String("");
		int i = 0;
		Method[] methods = clazz.getMethods();
		while (i < methods.length && !find) {
			Method m = methods[i];
			find = (m.getName().equalsIgnoreCase(methodName)) && m.getParameterTypes().length == paramsValues.length;
			if (find) {
				ret = kinvoke(m, o, paramsValues);
				break;
			}
			i++;
		}
		if (!find) {
			Class sClazz = clazz.getSuperclass();
			if (sClazz != null)
				ret = kinvoke(methodName, o, clazz.getSuperclass(), paramsValues);
		}
		return ret;
	}

	public static Object kinvoke(String methodName, Object o, Class clazz, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		boolean find = false;
		Object ret = new String("");
		int i = 0;
		Method[] methods = clazz.getMethods();
		while (i < methods.length && !find) {
			Method m = methods[i];
			find = (m.getName().equalsIgnoreCase(methodName)) && m.getParameterTypes().length == paramsValues.length;
			if (find) {
				ret = kinvoke(m, o, paramsValues);
				break;
			}
			i++;
		}
		if (!find) {
			Class sClazz = clazz.getSuperclass();
			if (sClazz != null)
				ret = kinvoke(methodName, o, clazz.getSuperclass(), paramsValues);
			else
				throw new IllegalAccessException("La méthode " + methodName + " ne peut être appelée pour la classe " + o.getClass());
		}
		return ret;
	}

	public static Object kinvoke(String methodName, String className, Object[] paramsValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class clazz = Class.forName(className);
		return kinvoke(methodName, clazz, clazz, paramsValues);
	}

	public static Object kinvoke(String methodName, Class clazz, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return kinvoke(methodName, (Object) clazz, paramsValues);
	}

	public Object invoke(String methodName, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return invoke(methodName, this.getClass(), paramsValues);
	}

	public Object invoke(String methodName, Class clazz, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return kinvoke(methodName, this, clazz, paramsValues);
	}

	public Object invoke(Method method, Object[] paramsValues) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return kinvoke(method, this, paramsValues);
	}

	public void setAttributes(Map<String, Object> map) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry pairs = (Map.Entry) iterator.next();
			try {
				__setAttribute((String) pairs.getKey(), pairs.getValue());
			} catch (Exception e) {
			}
		}
	}

	public void __setAttribute(String attribute, Object mixValue) throws SecurityException, IllegalAccessException, InvocationTargetException {
		__setAttribute(this, attribute, mixValue);
	}

	public static void __setAttribute(Object o, String attribute, Object mixValue) throws SecurityException, IllegalAccessException, InvocationTargetException {
		Field f = null;
		try {
			try {
				if (o instanceof Class)
					f = getField((Class) o, attribute);
				else
					f = getField(o.getClass(), attribute);
				f.setAccessible(true);
				f.set(o, mixValue);
			} catch (NoSuchFieldException nsfE) {
				Class cls = null;
				if (o != null)
					cls = o.getClass();
				KDebugConsole.print("Le membre de données " + attribute + " n'existe pas sur " + cls + " -> appel de " + attribute + "()", "PARSER", "KreflectObject.__setAttribute");
				kinvoke(attribute, o, new Object[] { mixValue });
			}
		} catch (IllegalArgumentException iaE) {
			String v = mixValue + "";
			Class cls = f.getType();
			KDebugConsole.print("Conversion de " + v + " en " + cls, "PARSER", "KreflectObject.__setAttribute");
			f.set(o, Converter.convert(v, cls));
		}
	}

	public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	public static Method getGetter(Class clazz, Field field) {
		String fieldName = field.getName();
		return getGetter(clazz, fieldName);
	}

	public static Method getGetter(Class clazz, String fieldName) {
		fieldName = KString.capitalizeFirstLetter(fieldName);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if ((method.getParameterTypes().length == 0)) {
				String methodName = method.getName();
				if (methodName.equals("get" + fieldName) || methodName.equals("is" + fieldName)) {
					return method;
				}
			}
		}
		return null;
	}

	public static ArrayList<String> getFieldNames(Class clazz) throws IllegalArgumentException, IllegalAccessException {
		ArrayList<String> result = new ArrayList<String>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isTransient(field.getModifiers()) || !field.getName().startsWith("_")) {
				result.add(field.getName());
			}
		}

		if (clazz.getSuperclass() != null && !clazz.getSuperclass().getSimpleName().startsWith("KObject")) {
			result.addAll(getFieldNames(clazz.getSuperclass()));
		}

		return result;
	}

	public static ArrayList<Field> getFields(Class clazz) throws IllegalArgumentException, IllegalAccessException {
		ArrayList<Field> result = new ArrayList<Field>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isTransient(field.getModifiers()) || !field.getName().startsWith("_")) {
				result.add(field);
			}
		}

		if (clazz.getSuperclass() != null && !clazz.getSuperclass().getSimpleName().startsWith("KObject")) {
			result.addAll(getFields(clazz.getSuperclass()));
		}

		return result;
	}

	public static Map<String, Field> getFieldsMap(Class clazz) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Field> result = new LinkedHashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isTransient(field.getModifiers()) || !field.getName().startsWith("_")) {
				result.put(field.getName(), field);
			}
		}

		if (clazz.getSuperclass() != null && !clazz.getSuperclass().getSimpleName().startsWith("KObject")) {
			result.putAll(getFieldsMap(clazz.getSuperclass()));
		}

		return result;
	}
}
