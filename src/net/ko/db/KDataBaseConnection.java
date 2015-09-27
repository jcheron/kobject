package net.ko.db;

import java.sql.SQLException;

import net.ko.bean.KDbParams;

public class KDataBaseConnection extends KDataBase {
	public KDataBaseConnection(KDbResultSet rs) {
		if (rs != null) {
			this.connection = rs.getConnection();
			KDataBase db = rs.getDatabase();
			if (db != null) {
				params = db.getParams();
				dbSpec = db.getDbSpec();
			}
		}
	}

	@Override
	protected void _connect() throws ClassNotFoundException, SQLException {

	}

	@Override
	public String getURL(KDbParams params) {
		return params + "";
	}

}
