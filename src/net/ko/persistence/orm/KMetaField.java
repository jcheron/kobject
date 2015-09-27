package net.ko.persistence.orm;

import java.lang.reflect.Field;

import net.ko.kobject.KObject;
import net.ko.persistence.annotation.Column;
import net.ko.persistence.annotation.KProcessAnnotation;

public class KMetaField {
	private Class<? extends KObject> clazz;
	private Field field;
	private String fieldName;
	private boolean unique;
	private boolean nullable;
	private boolean insertable;
	private boolean updatable;
	private String columnDefinition;
	private int length;
	private int precision;
	private int scale;
	private boolean primary;
	private boolean infoColumn;

	public KMetaField(Class<? extends KObject> clazz, Field field) {
		this.clazz = clazz;
		this.field = field;
		columnDefinition = "";
		fieldName = "";
		unique = false;
		nullable = false;
		insertable = true;
		updatable = true;
		length = 255;
		precision = 0;
		scale = 0;
		infoColumn = false;
		parse();
	}

	private void parse() {
		Column column = KProcessAnnotation.getColumnInfo(clazz, field);
		if (column != null) {
			columnDefinition = column.columnDefinition();
			fieldName = column.name();
			unique = column.unique();
			nullable = column.nullable();
			insertable = column.insertable();
			updatable = column.updatable();
			length = column.length();
			precision = column.precision();
			scale = column.scale();
			infoColumn = true;
		}
		primary = KProcessAnnotation.isId(field);
		if ("".equals(fieldName))
			fieldName = field.getName();
	}

	public Class<? extends KObject> getClazz() {
		return clazz;
	}

	public Field getField() {
		return field;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getMemberName() {
		return field.getName();
	}

	public boolean isUnique() {
		return unique;
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public int getLength() {
		return length;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}

	public boolean isPrimary() {
		return primary;
	}

	public String getFieldType() {
		String result = "";
		if (infoColumn)
			if (!"".equals(columnDefinition))
				result = columnDefinition;
		return result;
	}

	public KFieldSize getStringSize() {
		KFieldSize result = new KFieldSize();
		if (infoColumn)
			if (Column.defaultLength != length)
				result.setPrecision(length);
		return result;
	}

	public KFieldSize getDecimalSize() {
		KFieldSize result = new KFieldSize();
		if (infoColumn) {
			if (Column.defaultValue != precision)
				result.setPrecision(precision);
			if (Column.defaultValue != scale)
				result.setScale(scale);
		}
		return result;
	}
}
