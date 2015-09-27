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
import java.util.logging.Level;

import net.ko.bean.KDbParams;
import net.ko.db.KDBSpec;
import net.ko.db.KDataBase;
import net.ko.framework.Ko;

public class KDBPostgre extends KDataBase {

	public KDBPostgre(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		dbSpec = new KDBSpec("\"", "SERIAL PRIMARY KEY NOT NULL");
		dbSpec.setExpression("limit", " OFFSET {1} LIMIT {2}");
		driver = "org.postgresql.Driver";
		dbSpec.setAlterTableModifyColumn("ALTER TABLE {tableName} ALTER {fieldName} SET DATA TYPE {fieldType};\n");
	}

	public KDBPostgre() {
		this("127.0.0.1", "postgre", "", "", "5432");
	}

	public KDBPostgre(String host, String user, String pass, String base) {
		this(host, user, pass, base, "");
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "org.postgresql.Driver";
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
		String u = "jdbc:postgresql://" + params.getHost() + "/" + params.getBase();
		if (!params.getPort().equals(""))
			u = "jdbc:postgresql://" + params.getHost() + ":" + params.getPort() + "/" + params.getBase();
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		Ko.klogger().log(Level.INFO, "DB JDBC URL : " + u);
		return u;
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
	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		String result = super.javaTypeToDbType(aClazz);
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "BOOLEAN";
		return result;
	}

	@Override
	public Class<?> toJavaClass(String dbType, Class<?> _default) {
		Class<?> result = super.toJavaClass(dbType, _default);
		if (dbType.contains("serial"))
			result = int.class;
		return result;
	}

	@Override
	public boolean implementsCreateDb() {
		return true;
	}
}
