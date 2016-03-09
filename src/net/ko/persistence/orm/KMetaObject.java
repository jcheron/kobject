package net.ko.persistence.orm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.ko.inheritance.KReflectObject;
import net.ko.kobject.KFieldMap;
import net.ko.kobject.KObject;
import net.ko.persistence.annotation.KProcessAnnotation;
import net.ko.persistence.annotation.UniqueConstraint;

public class KMetaObject<T extends KObject> implements Serializable {
	private static final long serialVersionUID = 1L;
	protected Class<T> clazz;
	protected String tableName;
	protected List<String> KeyFields;
	protected KFieldMap fieldMap;
	protected Map<String, KMetaField> fields;
	protected UniqueConstraint[] uniqueConstraints;

	public KMetaObject(Class<T> clazz) {
		this.clazz = clazz;
		try {
			parse();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isEntity() {
		return KProcessAnnotation.isEntity(clazz);
	}

	private void parse() throws IllegalArgumentException, IllegalAccessException {
		tableName = KProcessAnnotation.getTableName(clazz);
		parseFields();
		parseKeyFields();
		uniqueConstraints = KProcessAnnotation.getUniqueConstraints(clazz);
	}

	private void parseKeyFields() {

		try {
			KeyFields = new ArrayList<>();
			if (fields == null) {
				parseFields();
			}
			for (KMetaField metaField : fields.values()) {
				Field field = metaField.getField();
				if (KProcessAnnotation.isId(field))
					KeyFields.add(field.getName());
			}
			if (KeyFields.size() == 0)
				KeyFields.add("id");
		} catch (IllegalArgumentException | IllegalAccessException e) {
		}
	}

	private void parseFields() throws IllegalArgumentException, IllegalAccessException {
		List<Field> listFields = KReflectObject.getFields(clazz);
		fields = new LinkedHashMap<String, KMetaField>();
		fieldMap = new KFieldMap();
		for (Field field : listFields) {
			if (!KProcessAnnotation.isTransient(field)) {
				KMetaField metaField = new KMetaField(clazz, field);
				String mappedFieldName = metaField.getFieldName();
				if (!field.getName().equals(mappedFieldName))
					fieldMap.addMap(field.getName(), mappedFieldName);
				fields.put(field.getName(), metaField);
			}
		}
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public List<String> getKeyFields() {
		return new ArrayList<>(KeyFields);
	}

	public KFieldMap getFieldMap() {
		return fieldMap;
	}

	public Map<String, KMetaField> getFields() {
		return fields;
	}

	public KMetaField getField(String fieldName) {
		return fields.get(fieldName);
	}

	public String getFieldName(String memberName) {
		String ret = memberName;
		if (fieldMap.existsMember(memberName))
			ret = fieldMap.getFieldName(memberName);
		return ret;
	}

	public String getMemberName(String fieldName) {
		String ret = fieldName;
		if (fieldMap.existsField(fieldName))
			ret = fieldMap.getMemberName(fieldName);
		return ret;
	}

	public UniqueConstraint[] getUniqueConstraints() {
		return uniqueConstraints;
	}

	/**
	 * Retourne le nom du premier membre faisant partie de la clé primaire
	 * 
	 * @return nom du membre clé primaire
	 */
	public String getFirstKey() {
		String result = "";
		List<String> keys = getKeyFields();
		if (keys != null && keys.size() > 0)
			result = keys.get(0);
		return result;
	}
}
