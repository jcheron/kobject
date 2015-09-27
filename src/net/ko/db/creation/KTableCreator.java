package net.ko.db.creation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.ko.db.KDataBase;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KObject;
import net.ko.persistence.annotation.KProcessAnnotation;
import net.ko.persistence.annotation.UniqueConstraint;
import net.ko.persistence.orm.KMetaField;
import net.ko.persistence.orm.KMetaObject;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KTableCreator {
	private final Class<? extends KObject> clazz;
	private KDataBase db;
	private Map<String, Object> fields;
	private KObject instance;
	private String tableName;
	private String PCHAR;

	public KTableCreator(Class<? extends KObject> clazz) {
		super();
		this.clazz = clazz;
		instance = Ko.getKoInstance(clazz);
		if (instance != null)
			tableName = instance.getTableName();
	}

	private void __setFields() {
		fields = new LinkedHashMap<String, Object>();
		KMetaObject<? extends KObject> metaObject = Ko.getMetaObject(instance.getClass());
		List<String> keyFields = metaObject.getKeyFields();
		if (keyFields.contains("id")) {
			Class<?> idClass = KProcessAnnotation.getIdClass(instance.getClass());
			if (Integer.class.equals(idClass) || int.class.equals(idClass))
				fields.put("id", autoInc(db.javaTypeToDbType(int.class)));
			else
				fields.put("id", db.javaTypeToDbType(idClass));
		}
		Map<String, KMetaField> metaFields = metaObject.getFields();
		try {
			for (KMetaField metaField : metaFields.values()) {

				String t = db.javaTypeToDbType(metaField);
				if (t != null) {
					String fieldName = metaField.getFieldName();
					if (keyFields.contains(fieldName))
						t = t + " NOT NULL";
					fields.put(fieldName, t);
				}
			}
		} catch (IllegalArgumentException e) {
			Ko.klogger().log(Level.WARNING, "Erreur de lecture des membres de l'instance " + instance, e);
		}

	}

	public Class<?> getJavaFieldType(String fieldName) throws NoSuchFieldException {
		return instance.getFieldType(fieldName);
	}

	private String autoInc(String v) {
		String result = v;
		result = db.AUTOINC().replace("{fieldDef}", v);
		return result;
	}

	public String getPKConstraint() {
		return alterTableAddPrimaryKey(db);
	}

	public String getSQLCreate() {
		return db.getDbSpec().createTable(instance, tableName, fields);
	}

	public String alterTableAddColumn(KDataBase db, String memberName) {
		String result = "";
		if (fields.containsKey(memberName)) {
			String info = fields.get(memberName) + "";
			result = db.getDbSpec().alterTableAddColumn(PCHAR + tableName + PCHAR, PCHAR + memberName + PCHAR + " " + info);
		}
		return result;
	}

	public String alterTableModifyColumn(KDataBase db, String memberName) {
		String result = "";
		if (fields.containsKey(memberName)) {
			String info = fields.get(memberName) + "";
			result = db.getDbSpec().alterTableModifyColumn(PCHAR + tableName + PCHAR, PCHAR + memberName + PCHAR, info);
		}
		return result;
	}

	public String alterTableDropColumn(KDataBase db, String memberName) {
		return db.getDbSpec().alterTableDropColumn(PCHAR + tableName + PCHAR, PCHAR + memberName + PCHAR);
	}

	public String alterTableDropIndex(KDataBase db, String indexName) {
		return db.getDbSpec().alterTableDropIndex(PCHAR + tableName + PCHAR, indexName);
	}

	public String alterTableAddPrimaryKey(KDataBase db) {
		List<String> keyFields = Ko.getMetaObject(instance.getClass()).getKeyFields();
		keyFields.remove("id");
		String result = "";
		if (keyFields.size() > 0)
			result = db.getDbSpec().alterTableAddPk(PCHAR + tableName + PCHAR, "PK_" + tableName, new KStrings(keyFields).implode_param(",", PCHAR, "", KGlueMode.VALUE, false));
		return result;
	}

	public String alterTableAddUniqueConstraints(KDataBase db) {
		String result = "";
		UniqueConstraint[] uniqueConstraints = Ko.getMetaObject(instance.getClass()).getUniqueConstraints();
		for (UniqueConstraint constraint : uniqueConstraints) {
			String[] names = constraint.columnNames();
			if (names.length > 0)
				result = db.getDbSpec().alterTableAddUnique(PCHAR + tableName + PCHAR, "U_" + tableName + KStrings.implode("_", names), KStrings.implode(",", names));
		}
		return result;
	}

	public String alterTableAddUniqueConstraint(KDataBase db, UniqueConstraint constraint) {
		String result = "";
		String[] names = constraint.columnNames();
		if (names.length > 0)
			result = db.getDbSpec().alterTableAddUnique(PCHAR + tableName + PCHAR, "U_" + tableName + KStrings.implode("_", names), KStrings.implode(",", names));
		return result;
	}

	public String addAutoIncrement() {
		List<String> keyFields = Ko.getMetaObject(instance.getClass()).getKeyFields();
		String result = "";
		if (keyFields.contains("id")) {
			result = db.getDbSpec().addExtraAutoIncrement(tableName, "id");
		}
		return result;
	}

	public String alterTableAddForeignKey(KDataBase db, KForeignKey fk) {
		return fk.getScript(db);
	}

	public void setDb(KDataBase db) {
		this.db = db;
		PCHAR = db.PCHAR();
		__setFields();
	}

	public List<KForeignKey> getForeignKeys() {
		List<KForeignKey> foreignKeys = new ArrayList<>();
		for (KConstraint constraint : instance.getConstraints()) {
			if (constraint.getForeignKey() != null) {
				KForeignKey fk = constraint.getForeignKey();
				fk.setTableName(tableName);
				foreignKeys.add(fk);
			}

		}
		return foreignKeys;
	}

	public UniqueConstraint[] getUniqueConstraints() {
		return Ko.getMetaObject(instance.getClass()).getUniqueConstraints();
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getRealFieldName(String memberName) {
		return instance.getFieldName(memberName);
	}

	public List<String> getJavaKeyFields() {
		return Ko.getMetaObject(instance.getClass()).getKeyFields();
	}

	public KObject getInstance() {
		return instance;
	}
}
