package net.ko.ksql;

import java.util.Map;

public class KParameterizedDelete extends KParameterizedExecute {
	public KParameterizedDelete(String quote, String tableName, Map<String, Object> fieldInCondition) {
		super(quote, tableName);
		addFieldsInCondition(fieldInCondition);
	}

	@Override
	public String parse() {
		String query = getSql();
		query = KSqlQuery.addWhere(query, condition);
		return query;
	}

	@Override
	public String getSql() {
		if (sql != null && !"".equals(sql))
			return sql;
		else
			return "DELETE FROM " + quote + tableName + quote;
	}

	@Override
	public String toString() {
		String result = parse();
		for (Object o : fields) {
			result = result.replaceFirst("\\?", "'" + o + "'");
		}
		return result;
	}
}
