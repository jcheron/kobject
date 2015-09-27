package net.ko.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.ko.dao.KDaoSpec;
import net.ko.kobject.KObject;
import net.ko.utils.KRegExpr;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KDBSpec extends KDaoSpec {

	protected String autoIncMask;
	protected String alterTableAddColumn;
	protected String alterTableModifyColumn;
	protected String alterTableDropColumn;
	protected String alterTableDropIndex;
	protected String alterTableAddPk;
	protected String alterTableAddFk;
	protected Map<String, String> expressions;

	public KDBSpec(String protectChar, String autoIncMask) {
		super(protectChar);
		this.autoIncMask = autoIncMask;
		this.alterTableAddColumn = "ALTER TABLE {tableName} ADD COLUMN {columnInfo};\n";
		this.alterTableModifyColumn = "ALTER TABLE {tableName} MODIFY {fieldName} {fieldType};\n";
		this.alterTableDropColumn = "ALTER TABLE {tableName} DROP {columnName};\n";
		this.alterTableDropIndex = "ALTER TABLE {tableName} DROP {indexName};\n";
		this.alterTableAddPk = "ALTER TABLE {tableName} ADD CONSTRAINT {pkName} PRIMARY KEY ({keyFields});\n";
		this.alterTableAddFk = "ALTER TABLE {tableName} ADD CONSTRAINT {fkName} FOREIGN KEY ({fkField}) REFERENCES {referencesTable}({referencesField}) ON DELETE {onDeleteRule} ON UPDATE {onUpdateRule};\n";
		expressions = new HashMap<String, String>();
		expressions.put("limit", " LIMIT {1},{2}");
		expressions.put("createTable", "CREATE TABLE {tableName} ({fieldDef});\n");
		expressions.put("fkName", "fk_{tableName}_{fkField}_{referencesTable}_{referencesField}");
		expressions.put("createDataBase", "CREATE DATABASE {dbName};\n");
		expressions.put("unique", "ALTER TABLE {tableName} ADD CONSTRAINT {ctName} UNIQUE ({fields});\n");
	}

	private ArrayList<String> getFields(String command, String sepFirst, String sepLast) {
		ArrayList<String> result = new ArrayList<String>();
		String m = Pattern.quote(command);
		result = KRegExpr.getGroups("\\" + sepFirst + "{1}((?:\\s|.)+?)\\" + sepLast + "{1}", m, 1);
		return result;
	}

	public String getCommand(String command, String... args) {
		ArrayList<String> fields = getFields(command, "{", "}");
		int i = 0;
		for (String field : fields) {
			String fieldC = "\\{" + field + "\\}";
			if (i < args.length)
				command = command.replaceAll(fieldC, args[i]);
			else
				command = command.replaceAll(fieldC, "");
			i++;
		}
		return command;
	}

	public String limit(int limit, int offset) {
		return getCommand(expressions.get("limit"), limit + "", offset + "");
	}

	public String addExtraAutoIncrement(String tableName, String keyField) {
		String result = "";
		if (expressions.containsKey("autoIncrement"))
			result = getCommand(expressions.get("autoIncrement"), tableName, keyField);
		return result;
	}

	public String createTable(KObject instance, String tableName, Map<String, Object> fields) {
		String fieldDef = KStrings.implode_param(fields, ",", "", " ", KGlueMode.KEY_AND_VALUE, protectChar, false);
		return getCommand(expressions.get("createTable"), protectChar + tableName + protectChar, fieldDef);
	}

	public String alterTableAddColumn(String... args) {
		return getCommand(alterTableAddColumn, args);
	}

	public String alterTableModifyColumn(String... args) {
		return getCommand(alterTableModifyColumn, args);
	}

	public String alterTableDropColumn(String... args) {
		return getCommand(alterTableDropColumn, args);
	}

	public String alterTableDropIndex(String... args) {
		return getCommand(alterTableDropIndex, args);
	}

	public String alterTableAddPk(String... args) {
		return getCommand(alterTableAddPk, args);
	}

	public String alterTableAddUnique(String... args) {
		String result = "";
		if (expressions.containsKey("unique"))
			result = getCommand(expressions.get("unique"), args);
		return result;
	}

	public String alterTableAddFk(String... args) {
		return getCommand(alterTableAddFk, args);
	}

	public String getAutoIncMask() {
		return autoIncMask;
	}

	public void setAutoIncMask(String autoIncMask) {
		this.autoIncMask = autoIncMask;
	}

	public String getAlterTableAddColumn() {
		return alterTableAddColumn;
	}

	public void setAlterTableAddColumn(String alterTableAddColumn) {
		this.alterTableAddColumn = alterTableAddColumn;
	}

	public String getAlterTableModifyColumn() {
		return alterTableModifyColumn;
	}

	public void setAlterTableModifyColumn(String alterTableModifyColumn) {
		this.alterTableModifyColumn = alterTableModifyColumn;
	}

	public String getAlterTableDropColumn() {
		return alterTableDropColumn;
	}

	public void setAlterTableDropColumn(String alterTableDropColumn) {
		this.alterTableDropColumn = alterTableDropColumn;
	}

	public String getAlterTableDropIndex() {
		return alterTableDropIndex;
	}

	public void setAlterTableDropIndex(String alterTableDropIndex) {
		this.alterTableDropIndex = alterTableDropIndex;
	}

	public String getAlterTableAddPk() {
		return alterTableAddPk;
	}

	public void setAlterTableAddPk(String alterTableAddPk) {
		this.alterTableAddPk = alterTableAddPk;
	}

	public String getAlterTableAddFk() {
		return alterTableAddFk;
	}

	public void setAlterTableAddFk(String alterTableAddFk) {
		this.alterTableAddFk = alterTableAddFk;
	}

	public void setExpression(String key, String value) {
		expressions.put(key, value);
	}

	public String getFkName(String tableName, String fkField, String referencesTable, String referencesField) {
		String result = "fk_" + tableName + "_" + fkField + "_" + referencesTable + "_" + referencesField;
		if (expressions.containsKey("fkName"))
			result = getCommand(expressions.get("fkName"), tableName, fkField, referencesTable, referencesField);
		return result;
	}

	public String createDataBase(String dbName) {
		String result = "";
		if (expressions.containsKey("createDataBase"))
			result = getCommand(expressions.get("createDataBase"), dbName);
		return result;
	}
}
