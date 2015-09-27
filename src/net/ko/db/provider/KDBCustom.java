package net.ko.db.provider;

import java.sql.DriverManager;
import java.sql.SQLException;

import net.ko.bean.KDbParams;
import net.ko.db.KDataBase;

public class KDBCustom extends KDataBase {
	private String url;

	public void connect(String driver, String url) throws ClassNotFoundException, SQLException {
		this.driver = driver;
		this.url = url;
		_connect();
	}

	@Override
	protected void _connect() throws ClassNotFoundException, SQLException {
		super._connect();
		Class.forName(driver);
		connection = DriverManager.getConnection(this.url);

	}

	@Override
	public String getURL(KDbParams params) {
		// TODO Auto-generated method stub
		return url;
	}
}
