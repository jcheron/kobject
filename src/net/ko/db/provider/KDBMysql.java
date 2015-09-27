/**
 * Classe KDBMysql
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
import java.sql.SQLException;

import net.ko.bean.KDbParams;
import net.ko.db.KDBSpec;
import net.ko.db.KDataBase;

public class KDBMysql extends KDataBase {

	public KDBMysql(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		driver = "com.mysql.jdbc.Driver";
		params.setOptions("jdbcCompliantTruncation=false");
		dbSpec = new KDBSpec("`", "{fieldDef} PRIMARY KEY AUTO_INCREMENT NOT NULL");
	}

	public KDBMysql(String host, String user, String pass, String base) {
		this(host, user, pass, base, "3306");
	}

	@Override
	protected synchronized void _connect() throws ClassNotFoundException, SQLException {
		super._connect();
		Class.forName(driver);
		connection = DriverManager.getConnection(getURL(), params.getUser(), params.getPass());
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		super.connect(host, user, pass, base, port, options);
		driver = "com.mysql.jdbc.Driver";
	}

	public KDBMysql() {
		this("localhost", "root", "", "", "3306");
	}

	@Override
	public String getURL(KDbParams params) {
		String u = "jdbc:mysql://" + params.getHost() + "/" + params.getBase();
		if (!params.getPort().equals(""))
			u = "jdbc:mysql://" + params.getHost() + ":" + params.getPort() + "/" + params.getBase();
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		return u;
	}

	@Override
	public boolean implementsCreateDb() {
		return true;
	}

}
