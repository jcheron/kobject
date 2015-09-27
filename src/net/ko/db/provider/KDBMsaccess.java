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

public class KDBMsaccess extends KDataBase {

	public KDBMsaccess(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		dbSpec = new KDBSpec("", "{fieldDef} COUNTER(0,1) NOT NULL CONSTRAINT PRIMARY KEY");
		driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		dbSpec.setExpression("limit", "");
	}

	public KDBMsaccess() {
		this("", "", "", "", "");
	}

	public KDBMsaccess(String host, String user, String pass, String base) {
		this(host, user, pass, base, "");
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "sun.jdbc.odbc.JdbcOdbcDriver";
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
		String base = params.getBase();
		base = base.replace("\\", "/");
		String u = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + base;
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		return u;
	}

	@Override
	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		String result = super.javaTypeToDbType(aClazz);
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
		connection = null;
	}

	@Override
	public String limit(int limit, int offset) {
		return "";
	}

}
