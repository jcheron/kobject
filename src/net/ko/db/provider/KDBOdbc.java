/**
 * Classe KDBOdbc
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2009
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDBOdbc.java,v 1.5 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db.provider;

import java.sql.DriverManager;
import java.sql.SQLException;

import net.ko.bean.KDbParams;
import net.ko.db.KDataBase;

public class KDBOdbc extends KDataBase {

	public KDBOdbc() {
		super();
		driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	}

	public KDBOdbc(String host, String user, String pass, String base) {
		super(host, user, pass, base);
		driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	}

	public KDBOdbc(String host, String user, String pass) {
		this(host, user, pass, "");
	}

	public KDBOdbc(String odbcSourceName) {
		this(odbcSourceName, "", "", "");
	}

	@Override
	protected void _connect() throws ClassNotFoundException, SQLException {
		super._connect();
		Class.forName(driver);
		connection = DriverManager.getConnection(getURL(), params.getUser(), params.getPass());
		params.setBase(connection.getCatalog());

	}

	@Override
	public String getURL(KDbParams params) {
		return "jdbc:odbc:" + params.getHost();
	}
}
