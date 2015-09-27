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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import net.ko.bean.KDbParams;
import net.ko.db.KDBSpec;
import net.ko.db.KDataBase;

public class KDBFirebird extends KDataBase {
	private int fkCounter = 0;

	public KDBFirebird(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		dbSpec = new KDBSpec("\"", "{fieldDef} PRIMARY KEY NOT NULL");
		driver = "org.firebirdsql.jdbc.FBDriver";
		dbSpec.setAlterTableModifyColumn("ALTER TABLE {tableName} ALTER COLUMN {fieldName} TYPE {fieldType};\n");
		dbSpec.setExpression("limit", " OFFSET {1} LIMIT {2}");
		dbSpec.setExpression("createTable", "CREATE TABLE {tableName} ({fieldDef});\n");
		dbSpec.setExpression("autoIncrement", "CREATE GENERATOR gen_{tableName}_{keyField};\n" +
				"SET GENERATOR gen_{tableName}_{keyField} TO 0;\n" +
				"set term !! ;\n" +
				"CREATE TRIGGER {tableName}_BI FOR {tableName}\n" +
				"ACTIVE BEFORE INSERT POSITION 0\n" +
				"AS\n" +
				"BEGIN\n" +
				"if (NEW.ID is NULL) then NEW.ID = GEN_ID(gen_{tableName}_{keyField}, 1);\n" +
				"END!!\n" +
				"set term ; !!\n");
		params.setOptions("");
	}

	public KDBFirebird() {
		this("127.0.0.1", "SYSDBA", "masterkey", "", "3050");
	}

	public KDBFirebird(String host, String user, String pass, String base) {
		this(host, user, pass, base, "");
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "org.firebirdsql.jdbc.FBDriver";
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
		String u = "jdbc:firebirdsql:" + params.getHost() + ":" + params.getBase();
		if (!params.getPort().equals(""))
			u = "jdbc:firebirdsql:" + params.getHost() + "/" + params.getPort() + ":" + params.getBase();
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		return u;
	}

	@Override
	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		String result = super.javaTypeToDbType(aClazz);
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "smallint";
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

	@Override
	public void close() throws SQLException {
		super.close();
	}

	@Override
	public String getFkName(String tableName, String fkField, String referencesTable, String referencesField) {
		fkCounter++;
		return "fk_" + fkCounter;
	}
}
