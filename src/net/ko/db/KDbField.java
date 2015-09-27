package net.ko.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.ko.creator.KClassCreator;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

public class KDbField {
	private boolean index = false;
	private String name;
	private String type;
	private boolean primary = false;
	private boolean autoInc = false;
	private boolean allowNull = true;
	private String _default;
	private String typeValues;
	private int numericPrecision;
	private int numericScale;
	private int columnSize = -1;
	private String javaType;
	private boolean primitive;

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public boolean isIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isPrimary() {
		return primary;
	}

	public boolean isAutoInc() {
		return autoInc;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public String get_default() {
		return _default;
	}

	public String getTypeValues() {
		return typeValues;
	}

	public int getNumericPrecision() {
		return numericPrecision;
	}

	public int getNumericScale() {
		return numericScale;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public KDbField(int columnIndex, ResultSetMetaData rsmd) throws SQLException {
		name = rsmd.getColumnName(columnIndex);
		type = rsmd.getColumnTypeName(columnIndex);
		columnSize = rsmd.getColumnDisplaySize(columnIndex);
		allowNull = rsmd.isNullable(columnIndex) == DatabaseMetaData.columnNullable;
		javaType = KDataTypeConverter.toJavaType(type, rsmd.getColumnClassName(columnIndex));
		autoInc = rsmd.isAutoIncrement(columnIndex);
		primitive = true;
	}

	public KDbField(String name, String javaType) {
		this.name = name;
		this.javaType = javaType;
		primitive = false;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public KDbField(ResultSet rs) throws SQLException {
		name = rs.getString("COLUMN_NAME");
		type = rs.getString("TYPE_NAME");
		columnSize = rs.getInt("COLUMN_SIZE");
		allowNull = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
		javaType = KDataTypeConverter.toJavaType(type);
		primitive = true;
	}

	public void setAutoInc(boolean autoInc) {
		this.autoInc = autoInc;
	}

	public String getAnnotation(String memberName, KClassCreator classCreator) {
		String result = "";
		if (primary) {
			classCreator.getImports().add("import net.ko.persistence.annotation.Id;\n");
			result = "\t@Id\n";
		}
		List<String> parts = new ArrayList<>();
		if (!memberName.equals(name))
			parts.add("name=\"" + name + "\"");
		if (parts.size() > 0) {
			classCreator.getImports().add("import net.ko.persistence.annotation.Column;\n");
			result += "\t@Column(" + KStrings.implode(",", parts, "") + ")\n";
		}
		return result;
	}

	public String getterName(String memberName) {
		String result = "get";
		if (javaType != null & "boolean".equalsIgnoreCase(javaType))
			result = "is";
		result += KString.capitalizeFirstLetter(memberName);
		return result;
	}

	public String toString() {
		return javaType;
	}

	public boolean isPrimitive() {
		return primitive;
	}
}
