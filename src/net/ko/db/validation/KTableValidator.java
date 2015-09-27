package net.ko.db.validation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.ko.db.KDBForeignKeyList;
import net.ko.db.KDataBase;
import net.ko.db.KDbField;
import net.ko.db.creation.KForeignKey;
import net.ko.db.creation.KTableCreator;
import net.ko.db.creation.KUniqueConstraint;
import net.ko.framework.Ko;
import net.ko.kobject.KObject;
import net.ko.persistence.annotation.UniqueConstraint;
import net.ko.utils.Converter;

public class KTableValidator {
	public enum RepareOperation {
		roAdd, roModify, roNone;
	}

	private KDataBase db;
	private KTableCreator tableCreator;
	private Map<String, KDbField> dbFields;
	private KDBForeignKeyList dbForeignKeys;
	private Map<String, RepareOperation> fieldsToRepare;
	private RepareOperation isValidPrimaryKey;
	private boolean isValidTable;
	private boolean autoRepare;
	private String repareScript;
	private String alterRepareScript;
	private String fkScript;

	public KTableValidator(Class<? extends KObject> clazz) {
		tableCreator = new KTableCreator(clazz);
		dbFields = new HashMap<>();
		dbForeignKeys = new KDBForeignKeyList();
		fieldsToRepare = new HashMap<>();
		isValidPrimaryKey = RepareOperation.roNone;
		isValidTable = true;
		autoRepare = false;
		repareScript = "";
		alterRepareScript = "";
		fkScript = "";
	}

	public KTableCreator getTableCreator() {
		return tableCreator;
	}

	public boolean checkTableExist() {
		boolean result = db.tableExist(tableCreator.getTableName());
		isValidTable = result;
		return result;
	}

	public boolean checkTableFields() {
		boolean result = true;
		String tableName = tableCreator.getTableName();
		try {
			dbFields = db.getFieldNamesAndTypes(tableName);
			for (String memberName : tableCreator.getFields().keySet()) {
				String realFieldName = tableCreator.getRealFieldName(memberName);
				if (dbFields.containsKey(realFieldName)) {
					try {
						KDbField dbField = dbFields.get(realFieldName.toUpperCase());
						Class<?> dbType = db.toJavaClass(dbField.getType(), String.class);

						Class<?> javaType = tableCreator.getJavaFieldType(memberName);
						if (!Converter.isTypeCompatible(javaType, dbType, false)) {
							fieldsToRepare.put(realFieldName, RepareOperation.roModify);
							Ko.klogger().log(Level.WARNING, "Le type " + javaType + " du membre " + memberName + " de la table " + tableName + " ne correspond pas à celui de la BDD : " + dbType);
							result = false;
						}
					} catch (NoSuchFieldException e) {
					}
				} else {
					fieldsToRepare.put(realFieldName, RepareOperation.roAdd);
					Ko.klogger().log(Level.WARNING, "Le champ " + realFieldName + " est absent de la table " + tableName + " dans la BDD");
					result = false;
				}
			}
		} catch (SQLException e) {
			result = false;
			Ko.klogger().log(Level.WARNING, "Erreur sur la vérification des champs de la table " + tableName, e);
		}
		return result;
	}

	public boolean checkTablePk() {
		boolean result = true;
		String tableName = tableCreator.getTableName();
		try {
			Set<String> dbKeyFields = db.getKeyFields(tableName);
			List<String> javaKeyFields = tableCreator.getJavaKeyFields();
			for (String memberName : javaKeyFields) {
				String realFieldName = tableCreator.getRealFieldName(memberName);
				if (!dbKeyFields.contains(realFieldName)) {
					isValidPrimaryKey = RepareOperation.roModify;
					result = false;
					if (dbKeyFields.size() == 0)
						isValidPrimaryKey = RepareOperation.roAdd;
					Ko.klogger().log(Level.WARNING, "Le champ " + realFieldName + " est absent dans la clé primaire de la table " + tableName);
				}
			}

		} catch (SQLException e) {
			result = false;
			Ko.klogger().log(Level.WARNING, "Erreur sur la vérification de la clé primaire de la table " + tableName, e);
		}

		return result;
	}

	public KDBForeignKeyList getDbForeignKeys() {
		String tableName = tableCreator.getTableName();
		try {
			return db.getForeignKeys(tableName);
		} catch (SQLException e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur la vérification des clés étrangères de la table " + tableName, e);
			return new KDBForeignKeyList();
		}
	}

	public boolean checkTableFk(KDBForeignKeyList dbForeignKeys) {
		boolean result = true;
		String tableName = tableCreator.getTableName();

		List<KForeignKey> foreignKeys = tableCreator.getForeignKeys();
		for (KForeignKey fk : foreignKeys) {
			if (!dbForeignKeys.contains(fk)) {
				fkScript += fk.getScript(db);
				Ko.klogger().log(Level.WARNING, "Contrainte de clé étrangère absente : " + fk + " dans la table " + tableName);
			}
		}

		return result;
	}

	public boolean checkTableUniqueIndexes(List<KUniqueConstraint> uniqueConstraints) {
		boolean result = false;
		UniqueConstraint[] uniqueConstraintsInClasses = tableCreator.getUniqueConstraints();
		for (UniqueConstraint constraint : uniqueConstraintsInClasses) {
			KUniqueConstraint c = new KUniqueConstraint(constraint.columnNames());
			if (!uniqueConstraints.contains(c)) {
				fkScript += tableCreator.alterTableAddUniqueConstraint(db, constraint);
				result = true;
			}
		}
		return result;
	}

	public void execute() {
		execute(false);
	}

	public void execute(boolean toRepare) {
		if (checkTableExist()) {
			checkTableFields();
			checkTablePk();
		} else {
			String tableName = tableCreator.getTableName();
			Ko.klogger().log(Level.WARNING, "La table " + tableName + " n'existe pas dans la base de données");
		}
		if (autoRepare || toRepare)
			repare();
	}

	public void repare() {
		if (!isValidTable) {
			String sql = tableCreator.getSQLCreate();
			repareScript += sql;
			alterRepareScript += tableCreator.getPKConstraint();
		} else {
			for (Map.Entry<String, RepareOperation> eRepare : fieldsToRepare.entrySet()) {
				if (eRepare.getValue().equals(RepareOperation.roAdd))
					alterRepareScript += tableCreator.alterTableAddColumn(db, eRepare.getKey());
				else {
					alterRepareScript += tableCreator.alterTableModifyColumn(db, eRepare.getKey());
				}
			}
			if (!isValidPrimaryKey.equals(RepareOperation.roNone)) {
				if (isValidPrimaryKey.equals(RepareOperation.roModify))
					alterRepareScript += tableCreator.alterTableDropIndex(db, "PRIMARY KEY");
				alterRepareScript += tableCreator.getPKConstraint();
			}
		}
	}

	public boolean isAutoRepare() {
		return autoRepare;
	}

	public void setAutoRepare(boolean autoRepare) {
		this.autoRepare = autoRepare;
	}

	public Map<String, RepareOperation> getFieldsToRepare() {
		return fieldsToRepare;
	}

	public RepareOperation isValidPrimaryKey() {
		return isValidPrimaryKey;
	}

	public boolean isValidTable() {
		return isValidTable;
	}

	public String getRepareScript() {
		return repareScript;
	}

	public KDataBase getDb() {
		return db;
	}

	public void setDb(KDataBase db) {
		this.db = db;
		tableCreator.setDb(db);
	}

	public String getAlterRepareScript() {
		return alterRepareScript;
	}

	public String getFkScript() {
		return fkScript;
	}

	public void setFkScript(String fkScript) {
		this.fkScript = fkScript;
	}
}
