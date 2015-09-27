package net.ko.persistence.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.ko.inheritance.KReflectObject;
import net.ko.kobject.KObject;
import net.ko.utils.KoUtils;

public class KProcessAnnotation {
	public static Class<?> getIdClass(Class<? extends KObject> clazz) {
		IdClass idClass = clazz.getAnnotation(IdClass.class);
		if (idClass != null)
			return idClass.value();
		return Integer.class;
	}

	public static boolean isEntity(Class<? extends KObject> clazz) {
		return getAnnotation(clazz, Entity.class) != null;
	}

	public static boolean isId(Field f) {
		return f.getAnnotation(Id.class) != null;
	}

	public static boolean isTransient(Field f) {
		return f.getAnnotation(Transient.class) != null;
	}

	public static String getColumnName(Class<? extends KObject> clazz, Field f) {
		String result = f.getName();
		Column column = getColumnInfo(clazz, f);
		if (column != null) {
			if (!"".equals(column.name()))
				result = column.name();
		}
		return result;
	}

	public static UniqueConstraint[] getUniqueConstraints(Class<? extends KObject> clazz) {
		UniqueConstraint[] constraints = new UniqueConstraint[] {};
		Table table = clazz.getAnnotation(Table.class);
		if (table != null)
			constraints = table.uniqueConstraints();
		return constraints;
	}

	public static Column getColumnInfo(Class<? extends KObject> clazz, Field f) {
		Column result = f.getAnnotation(Column.class);
		if (result == null) {
			Method m = KReflectObject.getGetter(clazz, f);
			if (m != null) {
				Column column = m.getAnnotation(Column.class);
				if (column != null)
					result = column;
			}
		}
		return result;
	}

	public static String getEntityName(Class<? extends KObject> clazz) {
		Entity entity = getAnnotation(clazz, Entity.class);
		String result = KoUtils.getTableName(clazz);
		if (entity != null)
			if (!"".equals(entity.name()))
				result = entity.name();
		return result;
	}

	public static String getTableName(Class<? extends KObject> clazz) {
		Table table = getAnnotation(clazz, Table.class);
		String result = KoUtils.getTableName(clazz);
		if (table != null)
			if (!"".equals(table.name()))
				result = table.name();
		return result;
	}

	private static <T extends Annotation> T getAnnotation(Class<? extends KObject> clazz, Class<T> annotationClazz) {
		return clazz.getAnnotation(annotationClazz);
	}
}
