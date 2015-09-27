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

public class KDBH2 extends KDataBase {

	public KDBH2(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		dbSpec = new KDBSpec("", "{fieldDef} AUTO_INCREMENT PRIMARY KEY NOT NULL");
		driver = "org.h2.Driver";
		dbSpec.setAlterTableModifyColumn("ALTER TABLE {tableName} ALTER {fieldName} {fieldType};\n");
		params.setOptions("DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=FALSE");
	}

	public KDBH2() {
		this("file", "sa", "", "", "");
	}

	public KDBH2(String host, String user, String pass, String base) {
		this(host, user, pass, base, "");
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "org.h2.Driver";
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
		String u = "jdbc:h2:" + params.getHost() + ":" + params.getBase();
		if (!params.getPort().equals(""))
			u = "jdbc:h2:" + params.getHost() + ":" + params.getPort() + ":" + params.getBase();
		if (!"".equals(params.getOptions()))
			u += ";" + params.getOptions();
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
	public void close() throws SQLException {
		super.close();
	}

	@Override
	public int free() throws SQLException {
		return connection.createStatement().executeUpdate("shutdown;");
	}
}
