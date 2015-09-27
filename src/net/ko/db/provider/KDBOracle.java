/**
 * Classe KDBOracle
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2009
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDBOracle.java,v 1.4 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db.provider;

import java.sql.DriverManager;
import java.sql.SQLException;

import net.ko.bean.KDbParams;
import net.ko.db.KDataBase;

public class KDBOracle extends KDataBase {
	public KDBOracle(String host, String user, String pass, String base) {
		this(host, user, pass, base, "1521");
	}

	public KDBOracle(String host, String user, String pass, String base, String port) {
		super(host, user, pass, base, port);
		driver = "oracle.jdbc.driver.OracleDriver";
	}

	@Override
	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		super.connect(host, user, pass, base, port, options);
		driver = "oracle.jdbc.driver.OracleDriver";
	}

	public KDBOracle() {
		super();
		driver = "oracle.jdbc.driver.OracleDriver";
		params.setPort("1521");
	}

	@Override
	protected void _connect() throws ClassNotFoundException, SQLException {
		super._connect();
		Class.forName(driver);

		connection = DriverManager.getConnection(getURL());

	}

	@Override
	public String getURL(KDbParams params) {
		String u = "jdbc:oracle:thin:" + params.getUser() + "/" + "pass" + "@" + params.getHost() + ":" + params.getPort() + "/" + params.getBase();
		if (!"".equals(params.getOptions()))
			u += "?" + params.getOptions();
		return u;
	}
}
