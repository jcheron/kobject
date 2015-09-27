package net.ko.db.creation;

import net.ko.db.KDBForeignKey;
import net.ko.db.KDBForeignKeyDef;
import net.ko.db.KDataBase;

public class KForeignKey {
	private String fkName;
	private String fkField;
	private String tableName;
	private String referencesTable;
	private String referencesField;
	private KDBForeignKeyDef onDeleteAction;
	private KDBForeignKeyDef onUpdateAction;

	public KForeignKey(String fkField, String referencesTable, String referencesField) {
		super();
		this.fkField = fkField;
		this.referencesTable = referencesTable;
		this.referencesField = referencesField;
		onDeleteAction = KDBForeignKeyDef.kdNoAction;
		onUpdateAction = KDBForeignKeyDef.kdNoAction;
	}

	public String getFkName(KDataBase db) {
		String result = fkName;
		if (result == null || "".equals(result))
			result = db.getFkName(tableName, fkField, referencesTable, referencesField);
		return result;
	}

	public void setFkName(String fkName) {
		this.fkName = fkName;
	}

	public String getFkField() {
		return fkField;
	}

	public void setFkField(String fkField) {
		this.fkField = fkField;
	}

	public String getReferencesTable() {
		return referencesTable;
	}

	public void setReferencesTable(String referencesTable) {
		this.referencesTable = referencesTable;
	}

	public String getReferencesField() {
		return referencesField;
	}

	public void setReferencesField(String referencesField) {
		this.referencesField = referencesField;
	}

	public String getScript(KDataBase db) {
		String PCHAR = db.PCHAR();
		String result = "";
		if (db.getDbSpec() != null)
			result = db.getDbSpec().alterTableAddFk(PCHAR + tableName + PCHAR, db.getFkName(tableName, fkField, referencesTable, referencesField), PCHAR + fkField + PCHAR, PCHAR + referencesTable + PCHAR, PCHAR + referencesField + PCHAR, onDeleteAction.getCaption(), onUpdateAction.getCaption());
		return result;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof KDBForeignKey) {
			KDBForeignKey dbFk = (KDBForeignKey) o;
			result = dbFk.getPkFieldName().equalsIgnoreCase(referencesField) && dbFk.getPkTableName().equalsIgnoreCase(referencesTable) && dbFk.getFkFieldName().equalsIgnoreCase(fkField) && dbFk.getFkTableName().equalsIgnoreCase(tableName);
		}
		if (o instanceof KForeignKey) {
			KForeignKey fk = (KForeignKey) o;
			result = fk.getReferencesField().equalsIgnoreCase(referencesField) && fk.getReferencesTable().equalsIgnoreCase(referencesTable) && fk.getFkField().equalsIgnoreCase(fkField) && fk.getTableName().equalsIgnoreCase(tableName);
		}
		return result;
	}

	@Override
	public String toString() {
		return "FK [" + fkField + " References -> " + referencesTable + "." + referencesField + "]";
	}

	public KDBForeignKeyDef getOnDeleteAction() {
		return onDeleteAction;
	}

	public void setOnDeleteAction(KDBForeignKeyDef onDeleteAction) {
		this.onDeleteAction = onDeleteAction;
	}

	public KDBForeignKeyDef getOnUpdateAction() {
		return onUpdateAction;
	}

	public void setOnUpdateAction(KDBForeignKeyDef onUpdateAction) {
		this.onUpdateAction = onUpdateAction;
	}

}
