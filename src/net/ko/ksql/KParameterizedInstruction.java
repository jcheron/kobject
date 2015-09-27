package net.ko.ksql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.ko.utils.KStrings;

public class KParameterizedInstruction {
	protected String condition;
	protected String tableName;
	protected String sql;
	protected String orderBy = "";
	protected String limit = "";
	protected List<Object> fields;
	protected String quote;
	protected String refCondition;

	public KParameterizedInstruction(String quote) {
		this(quote, "", new ArrayList<Object>());
	}

	public KParameterizedInstruction(String quote, String baseSql, String condition, Object... fieldsValuesInCondition) {
		this(quote, baseSql, condition, new ArrayList<>(Arrays.asList(fieldsValuesInCondition)));
	}

	public KParameterizedInstruction(String quote, String baseSql, String condition, List<Object> fieldsValuesInCondition) {
		this.sql = baseSql;
		this.condition = condition;
		this.fields = fieldsValuesInCondition;
		this.quote = quote;
		this.refCondition = condition;
	}

	public KParameterizedInstruction(String quote, String condition, List<Object> fieldsValuesInCondition) {
		this(quote, "", condition, fieldsValuesInCondition);
	}

	public KParameterizedInstruction(String quote, Map<String, Object> fieldsInCondition) {
		this.fields = new ArrayList<>(fieldsInCondition.values());
		this.quote = quote;
		condition = KStrings.implodeAndReplaceValues(fieldsInCondition, " ? ", " AND ", "", "=", quote);
	}

	public KParameterizedInstruction(String quote, String condition) {
		this(quote, condition, new ArrayList<Object>());
	}

	public KParameterizedInstruction(String quote, String tableName, String condition) {
		this(quote, condition, new ArrayList<Object>());
		this.tableName = tableName;
	}

	private String getSelect() {
		String result = sql;
		if (tableName != null && !"".equals(tableName)) {
			result = KSqlQuery.makeSelect(quote + tableName + quote);
		}
		return result;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public List<Object> getFields() {
		return fields;
	}

	public void addFieldsInCondition(Map<String, Object> fields) {
		addFields(KStrings.implodeAndReplaceValues(fields, " ? ", " AND ", "", "=", quote), fields);
		refCondition = KStrings.implodeAndReplaceValues(fields, " ? ", " AND ", "", "=", "_quote_");
	}

	public void addFields(String where, Map<String, Object> fields) {
		condition = KSqlQuery.addWhere(condition, where);
		this.fields.addAll(new ArrayList<>(fields.values()));
	}

	public void addFields(List<Object> parameters) {
		if (fields == null)
			fields = new ArrayList<>();
		fields.addAll(parameters);
	}

	public void addFields(Map<String, Object> fields) {
		this.fields.addAll(new ArrayList<>(fields.values()));
	}

	public void addFieldsInCondition(Map<String, Object> fields, String glue) {
		refCondition = KStrings.implodeAndReplaceValues(fields, " ? ", " " + glue + " ", "", "=", "_quote_");
		addFields(KStrings.implodeAndReplaceValues(fields, " ? ", " " + glue + " ", "", "=", quote), fields);
	}

	public void addFields(Map<String, Object> fields, String glue, String equals) {
		refCondition = KStrings.implodeAndReplaceValues(fields, " ? ", " " + glue + " ", "", equals, "_quote_");
		addFields(KStrings.implodeAndReplaceValues(fields, " ? ", " " + glue + " ", "", equals, quote), fields);
	}

	public String getQuote() {
		return quote;
	}

	public void setQuote(String quote) {
		if (refCondition != null && !"".equals(this.refCondition))
			condition = refCondition.replace("_quote_", quote);
		this.quote = quote;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String parse() {
		String select = getSelect();
		if (condition != null && !"".equals(condition))
			select = KSqlQuery.addWhere(select, condition);
		if (select != null)
			if (select.toLowerCase().startsWith("select")) {
				if (orderBy != null && !"".equals(orderBy))
					select = KSqlQuery.addOrderBy(select, orderBy);
				select += limit;
			}
		return select;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getParameters() {
		return fields;
	}

	public void setFields(List<Object> fields) {
		this.fields = fields;
	}
}
