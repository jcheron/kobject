package net.ko.ksql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.ko.utils.KArray;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KParameterizedInsertInto extends KParameterizedExecute {
	private Map<String, Object> fieldsToAdd;

	public KParameterizedInsertInto(String quote, String tableName, Map<String, Object> fieldRow) {
		super(quote, tableName);
		fieldsToAdd = fieldRow;
	}

	@Override
	public String parse() {
		String query = getSql();
		return query;
	}

	private String getParamValues() {
		Object[] values = KArray.arrayOfSameValue("?", fieldsToAdd.size());
		return KStrings.implode(",", values);
	}

	@Override
	public String getSql() {
		if (sql != null && !"".equals(sql))
			return sql;
		else
			return "INSERT INTO " + quote + tableName + quote + "(" + KStrings.implode_param(fieldsToAdd, ",", quote, KGlueMode.KEY, false) + ") VALUES(" + getParamValues() + ")";
	}

	@Override
	public List<Object> getParameters() {
		return new ArrayList<>(fieldsToAdd.values());
	}

	@Override
	public void addFields(Map<String, Object> fields) {
		fieldsToAdd.putAll(fields);
	}

	@Override
	public String toString() {
		String result = getSql();
		for (Map.Entry<String, Object> e : fieldsToAdd.entrySet()) {
			result = result.replaceFirst("\\?", "'" + e.getValue() + "'");
		}
		return result;
	}

}
