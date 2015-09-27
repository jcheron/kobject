/**
 * Classe KDBHsqldb
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDBMysql.java,v 1.4 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db.provider;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.ko.bean.KDbParams;
import net.ko.db.KDBSpec;
import net.ko.db.KDataBase;
import net.ko.db.creation.KForeignKey;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KObject;
import net.ko.utils.KStrings;

public class KDBSqlite extends KDataBase {

	public KDBSqlite(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		dbSpec = new KDBSpec("\"", "{fieldDef} PRIMARY KEY AUTOINCREMENT NOT NULL") {

			@Override
			public String createTable(KObject instance, String tableName, Map<String, Object> fields) {
				String result = super.createTable(instance, tableName, fields);
				List<String> keys = Ko.getMetaObject(instance.getClass()).getKeyFields();
				if (keys.size() != 1 || !keys.contains("id")) {
					String pk = KStrings.implode(",", keys, protectChar);
					if (!"".equals(pk))
						pk = ", PRIMARY KEY (" + pk + ")";
					result += pk;
				}
				String fks = getFKsAsString(instance, tableName);
				result += fks + ");";
				return result;
			}

		};
		driver = "org.sqlite.JDBC";
		dbSpec.setAlterTableModifyColumn("");
		dbSpec.setAlterTableAddPk("");
		dbSpec.setAlterTableAddFk("");
		dbSpec.setExpression("limit", " LIMIT {1} OFFSET {2}");
		dbSpec.setExpression("createTable", "CREATE TABLE {tableName} ({fieldDef}");
	}

	public KDBSqlite() {
		this("", "", "", "", "");
	}

	public KDBSqlite(String host, String user, String pass, String base) {
		this(host, user, pass, base, "");
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "org.sqlite.JDBC";
	}

	@Override
	protected synchronized void _connect() throws ClassNotFoundException, SQLException {
		super._connect();
		Class.forName(driver);
		System.out.println(System.getProperty("dbPath"));
		connection = DriverManager.getConnection(getURL(), params.getUser(), params.getPass());
	}

	@Override
	public String getURL(KDbParams params) {
		String host = params.getHost();
		String base = params.getBase();
		if (!"".equals(host) && "".equals(base)) {
			host = ":memory:";
			base = "";
		}
		else
			host = "";
		String u = "jdbc:sqlite:" + host + base;
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		return u;
	}

	@Override
	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		String result = super.javaTypeToDbType(aClazz);
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "INTEGER";
		return result;
	}

	@Override
	public Set<String> getKeyFields(String tableName) throws SQLException {
		ResultSet clefs = null;
		Set<String> ret = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		try {
			clefs = connection.getMetaData().getPrimaryKeys(null, null, tableName);
			while (clefs.next()) {
				ret.add(clefs.getString("COLUMN_NAME"));
			}
		} finally {
			if (clefs != null)
				clefs.close();
		}
		return ret;
	}

	private List<KForeignKey> getForeignKeys(KObject instance, String tableName) {
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

	private String getOneFKasString(KForeignKey fk) {
		String quote = QUOTE();
		return dbSpec.getCommand("FOREIGN KEY ({fkField}) REFERENCES {referencesTable}({referencesField})", quote + fk.getFkField() + quote, quote + fk.getReferencesTable() + quote, quote + fk.getReferencesField() + quote);
	}

	private String getFKsAsString(KObject instance, String tableName) {
		String result = "";
		List<KForeignKey> foreignKeys = getForeignKeys(instance, tableName);
		for (KForeignKey fk : foreignKeys) {
			result += "," + getOneFKasString(fk);
		}
		return result;

	}

	@Override
	public int executeUpdate(PreparedStatement st) throws SQLException {
		int result = st.executeUpdate();
		return result;
	}

	@Override
	public void close() throws SQLException {
		super.close();
		connection = null;
	}

	@Override
	public String limit(int limit, int offset) {
		return super.limit(offset, limit);
	}

}
