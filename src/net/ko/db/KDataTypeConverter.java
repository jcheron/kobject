/**
 * Classe KDataTypeConverter
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDataTypeConverter.java,v 1.4 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db;

import java.util.Date;

public class KDataTypeConverter {
	public static String toDbType(Class<? extends Object> aClazz) {
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "BIT";
		if (aClazz.equals(Integer.class) || aClazz.equals(int.class))
			return "INTEGER";
		if (aClazz.equals(Long.class) || aClazz.equals(long.class))
			return "BIGINT";
		if (aClazz.equals(Short.class) || aClazz.equals(short.class))
			return "SMALLINT";
		if (aClazz.equals(Double.class) || aClazz.equals(double.class))
			return "DOUBLE";
		if (aClazz.equals(Float.class) || aClazz.equals(float.class))
			return "REAL";
		if (aClazz.equals(Byte.class) || aClazz.equals(byte.class))
			return "TINYINT";
		if (aClazz.equals(Date.class) || aClazz.equals(java.sql.Date.class))
			return "DATE";
		if (aClazz.equals(java.sql.Time.class))
			return "TIME";
		if (aClazz.equals(java.sql.Timestamp.class))
			return "timestamp";
		if (aClazz.equals(String.class))
			return "VARCHAR(50)";
		if (aClazz.equals(char.class) || aClazz.equals(Character.class))
			return "VARCHAR(1)";
		return null;
	}

	public static boolean isSerializable(Class<? extends Object> aClazz) {
		return toDbType(aClazz) != null;
	}

	public static String toJavaType(String dbType, String _default) {

		dbType = dbType.toLowerCase();
		if (dbType.equalsIgnoreCase("bit") || dbType.contains("bool") || dbType.equalsIgnoreCase("tinyint"))
			return "boolean";
		if (dbType.equalsIgnoreCase("bigint"))
			return "long";
		if (dbType.toLowerCase().contains("smallint") || dbType.contains("tinyint") || dbType.contains("mediumint") || dbType.contains("serial"))
			return "int";
		if (dbType.equalsIgnoreCase("integer") || dbType.contains("int"))
			return "int";
		if (dbType.equalsIgnoreCase("double"))
			return "double";
		if (dbType.toLowerCase().contains("float") || dbType.equalsIgnoreCase("real") || dbType.contains("decimal"))
			return "float";
		if (dbType.equalsIgnoreCase("date") || dbType.equalsIgnoreCase("datetime"))
			return "java.sql.Date";
		if (dbType.equalsIgnoreCase("time"))
			return "java.sql.Time";
		if (dbType.contains("timestamp"))
			return "java.sql.Timestamp";
		if (dbType.equalsIgnoreCase("year"))
			return "int";
		if (dbType.contains("enum") || dbType.contains("set"))
			return "String";
		if (dbType.equalsIgnoreCase("blob"))
			return "String";
		if (dbType.equalsIgnoreCase("text") || dbType.contains("char") || dbType.equalsIgnoreCase("enum"))
			return "String";

		return _default;
	}

	public static Class<?> toJavaClass(String dbType, Class<?> _default) {
		dbType = dbType.toLowerCase();
		if (dbType.equalsIgnoreCase("bit") || dbType.toLowerCase().contains("bool") || dbType.equalsIgnoreCase("tinyint"))
			return boolean.class;
		if (dbType.equalsIgnoreCase("bigint"))
			return long.class;
		if (dbType.toLowerCase().contains("smallint") || dbType.contains("tinyint") || dbType.contains("mediumint"))
			return int.class;
		if (dbType.equalsIgnoreCase("integer") || dbType.contains("int"))
			return int.class;
		if (dbType.equalsIgnoreCase("double"))
			return double.class;
		if (dbType.toLowerCase().contains("float") || dbType.equalsIgnoreCase("real") || dbType.contains("decimal"))
			return float.class;
		if (dbType.equalsIgnoreCase("date") || dbType.equalsIgnoreCase("datetime"))
			return java.sql.Date.class;
		if (dbType.equalsIgnoreCase("time"))
			return java.sql.Time.class;
		if (dbType.toLowerCase().contains("timestamp"))
			return java.sql.Timestamp.class;
		if (dbType.equalsIgnoreCase("year"))
			return int.class;
		if (dbType.contains("enum") || dbType.contains("set"))
			return String.class;
		if (dbType.equalsIgnoreCase("blob"))
			return String.class;
		if (dbType.equalsIgnoreCase("text") || dbType.contains("char") || dbType.equalsIgnoreCase("enum"))
			return String.class;

		return _default;
	}

	@SuppressWarnings("rawtypes")
	public static String getControllerType(String javaType) {
		String result = "";
		Class cls = null;
		try {
			cls = Class.forName(javaType);
			result = cls.getSimpleName().toLowerCase();
		} catch (Exception e) {
			result = javaType.toLowerCase();

		}
		return result;
	}

	public static String toJavaType(String dbType) {
		return toJavaType(dbType, "String");
	}
}
