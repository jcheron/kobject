/**
 * Classe KDbResultSet
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2009
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDbResultSet.java,v 1.5 2010/01/21 00:53:03 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import net.ko.framework.Ko;

public class KDbResultSet {
	private ResultSet resultSet;
	private KDataBase database;

	public Map<String, Object> toMap() throws SQLException {
		ResultSetMetaData rsmdt = resultSet.getMetaData();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (int i = 1; i <= rsmdt.getColumnCount(); i++) {
			Object o = null;
			try {
				o = resultSet.getObject(i);
			} catch (Exception e) {
			}
			if (o != null)
				map.put(rsmdt.getColumnLabel(i), o);
		}
		return map;
	}

	public Object getObject(int columnIndex) throws SQLException {
		return resultSet.getObject(columnIndex);
	}

	public Object getObject(String columnName) throws SQLException {
		return resultSet.getObject(columnName);
	}

	public KDbResultSet(ResultSet resultSet) {
		super();
		this.resultSet = resultSet;
	}

	public KDbResultSet(ResultSet resultSet, KDataBase db) {
		this(resultSet);
		database = db;
	}

	public boolean isBeforeFirst() throws SQLException {
		return resultSet.isBeforeFirst();
	}

	public boolean isAfterLast() throws SQLException {
		return resultSet.isAfterLast();
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public Connection getConnection() {
		try {
			return resultSet.getStatement().getConnection();
		} catch (SQLException e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'accéder à la connexion du resultset", e);
		}
		return null;
	}

	public KDataBase getDatabase() {
		return database;
	}

	public void close() throws SQLException {
		if (resultSet != null)
			resultSet.close();
	}

	public void setDatabase(KDataBase database) {
		this.database = database;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
}