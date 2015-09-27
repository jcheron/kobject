package net.ko.ksql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KSqlQuery {
	public static String getSimpleTableName(String query) {
		return KSqlQuery.getTableName(query).replaceAll("\\W", "");
	}

	public static String getTableName(String query) {
		String ret = query;
		Pattern p = Pattern.compile("^.*from[\\s]+([\\w]*)[\\s|\\,]*.*$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(query);
		boolean b = m.matches();

		if (b) {
			if (m.groupCount() >= 1) {
				ret = m.group(1);
			}
		}
		return ret;
	}

	public static String getSelectWithNewFields(String query, String newFieldsInSelect) {
		String ret = query;
		Pattern p = Pattern.compile("^(select[\\s]+)(.+?)([\\s]+from.*)$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(query);

		if (m.find()) {
			ret = m.replaceFirst("$1" + newFieldsInSelect + "$3");
		}
		return ret;
	}

	public static String checkWhere(String where) {
		String ret = where;
		if (where != "")
			if (!where.toUpperCase().trim().startsWith("WHERE") && !"".equals(where))
				ret = " WHERE " + where;
		return ret;
	}

	public static String removeWhere(String where) {
		String ret = where;
		int whereIndex = where.toLowerCase().indexOf("where");
		if (whereIndex != -1)
			ret = where.substring(whereIndex + 5);
		return ret;
	}

	public static String addWhere(String query, String where) {
		String ret = query;
		if (!where.equals("")) {
			int whereIndex = query.toLowerCase().indexOf("where");
			if (whereIndex != -1)
				ret += " AND " + KSqlQuery.removeWhere(where);
			else
				ret += " " + KSqlQuery.checkWhere(where);
		}
		return ret;
	}

	public static String makeSelect(String query) {
		String ret = query;
		if (!query.toLowerCase().startsWith("select "))
			ret = "SELECT * FROM " + query;
		return ret;
	}

	private static String checkClause(String strSql, String clause) {
		String ret = strSql;
		if (!"".equals(strSql))
			if (!strSql.toLowerCase().contains(clause.toLowerCase()))
				ret = clause + " " + strSql;
		return ret;
	}

	public static String removeClause(String strSql, String clause) {
		String ret = strSql;
		int clauseIndex = strSql.toLowerCase().indexOf(clause.toLowerCase());
		if (clauseIndex != -1)
			ret = strSql.replace(KSqlQuery.getClause(strSql, clause), "");
		return ret;
	}

	public static String addClause(String strSqlStart, String strSqlEnd, String clause) {
		return addClause(strSqlStart, strSqlEnd, clause, ",", false);
	}

	public static String addClause(String strSqlStart, String strSqlEnd, String clause, String sep, boolean keep) {
		String ret = strSqlStart;
		if (!keep)
			ret = KSqlQuery.getQuery(strSqlStart);
		if (!"".equals(strSqlEnd)) {
			int clauseIndex = strSqlStart.toLowerCase().indexOf(clause.toLowerCase());
			if (clauseIndex != -1)
				ret += " " + sep + " " + KSqlQuery.removeClause(strSqlEnd, clause);
			else
				ret += " " + KSqlQuery.checkClause(strSqlEnd, clause);
		}
		return ret;
	}

	public static String getQuery(String sql) {
		String result = "";
		Pattern p = Pattern.compile("^(.+?)(?:WHERE|ORDER BY|GROUP BY|LIMIT|HAVING|$)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		if (m.find())
			if (m.groupCount() > 0)
				result = m.group(1);
		return result;
	}

	public static String getCountQuery(String sql) {
		String result = "";
		Pattern p = Pattern.compile("^(.+?)(?:ORDER BY|GROUP BY|LIMIT|$)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		if (m.find())
			if (m.groupCount() > 0)
				result = m.group(1);
		return result;
	}

	public static String getWhere(String sql) {
		String result = "";
		Pattern p = Pattern.compile("(WHERE)\\s(.+?)+(ORDER BY|GROUP BY|HAVING|$)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		if (m.find())
			if (m.groupCount() > 0)
				result = m.group(1);
		return result;
	}

	public static String getClause(String sql, String clause) {
		String result = "";
		Pattern p = Pattern.compile("(" + clause + ")\\s(.+?)+(WHERE|ORDER BY|GROUP BY|LIMIT|HAVING|$)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		if (m.find())
			if (m.groupCount() > 1)
				result = m.group(2);
		return result;
	}

	public static String addOrderBy(String strSqlStart, String orderBy) {
		return KSqlQuery.addClause(strSqlStart, orderBy, "ORDER BY", ",", true);
	}
}
