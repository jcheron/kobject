package net.ko.ksql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.ko.utils.KStrings;

public class KParameterizedUpdate extends KParameterizedExecute {
	private Map<String, Object> fieldsToUpdate;

	public KParameterizedUpdate(String quote, String tableName, Map<String, Object> fieldRow, Map<String, Object> fieldCondition) {
		super(quote, tableName);
		this.fieldsToUpdate = fieldRow;
		this.addFieldsInCondition(fieldCondition);
	}

	@Override
	public String parse() {
		String query = getSql();
		if (condition != null && !"".equals(condition))
			query = KSqlQuery.addWhere(query, condition);
		return query;
	}

	@Override
	public String getSql() {
		if (sql != null && !"".equals(sql))
			return sql;
		else
			return "UPDATE " + quote + tableName + quote + " SET " + KStrings.implodeAndReplaceValues(fieldsToUpdate, "?", ",", "", "=", quote);
	}

	@Override
	public List<Object> getParameters() {
		List<Object> result = new ArrayList<>(fieldsToUpdate.values());
		result.addAll(fields);
		return result;
	}

	@Override
	public String toString() {
		String result = parse();
		for (Map.Entry<String, Object> e : fieldsToUpdate.entrySet()) {
			result = result.replaceFirst("\\?", "'" + e.getValue() + "'");
		}
		for (Object o : fields) {
			result = result.replaceFirst("\\?", "'" + o + "'");
		}
		return result;
	}
}
