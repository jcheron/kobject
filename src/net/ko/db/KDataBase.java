/**
 * Classe KDataBase
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KDataBase.java,v 1.8 2011/01/14 01:12:55 jcheron Exp $
 * @package net.ko.ksql
 */
package net.ko.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import net.ko.bean.KDbParams;
import net.ko.db.creation.KUniqueConstraint;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.ksql.KSqlQuery;
import net.ko.persistence.orm.KMetaField;
import net.ko.utils.Converter;
import net.ko.utils.KString;

public abstract class KDataBase {
	protected KDbParams params;

	protected Connection connection;
	protected String driver;
	@SuppressWarnings("unused")
	private static KDataBase instance;
	private ResultSet result;
	protected Statement statement;
	@SuppressWarnings("unused")
	private String query;
	protected boolean inuse;
	protected long timestamp;
	protected KDbConnectionPool pool = null;
	protected KDBSpec dbSpec;
	protected ArrayList<String> generatedKeys;
	protected boolean supportsTransactions;

	protected synchronized void _connect() throws ClassNotFoundException, SQLException {
		Ko.klogger().log(Level.WARNING, "Connexion à la base de données :" + getURL());
	}

	public static boolean isValid(Connection connection) {
		if (connection == null) {
			return false;
		}
		ResultSet ping = null;
		Statement st = null;
		try {
			if (connection.isClosed()) {
				return false;
			}
			st = connection.createStatement();
			ping = st.executeQuery("SELECT 1");
			return ping.next();
		} catch (SQLException sqle) {
			return false;
		} finally {
			if (ping != null) {
				try {
					ping.close();
					st.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public static boolean isValid(KDataBase db) {
		if (db != null)
			return KDataBase.isValid(db.getConnection());
		else
			return false;
	}

	public boolean isValid() {
		return KDataBase.isValid(connection);
	}

	protected Statement getStatement() throws SQLException {
		return getStatement(false);
	}

	protected Statement getStatement(boolean other) throws SQLException {
		if (statement == null || other || statement.isClosed())
			statement = connection.createStatement();
		return statement;
	}

	public PreparedStatement getPreparedStatement(String sql, int areGeneratedKeys) throws SQLException {
		return connection.prepareStatement(sql, areGeneratedKeys);
	}

	public KDataBase() {
		timestamp = 0;
		inuse = false;
		params = new KDbParams();
	}

	public KDataBase(String host, String user, String pass, String base) {
		super();
		params = new KDbParams(host, user, pass);
		params.setBase(base);
	}

	public KDataBase(String host, String user, String pass, String base, String port) {
		super();
		params = new KDbParams(host, user, pass, base, port);

	}

	public void connect(String host, String user, String pass, String base) throws ClassNotFoundException, SQLException {
		getDbParams().setParams(host, user, pass, base);
		_connect();
	}

	public void connect(String host, String user, String pass, String base, String port) throws ClassNotFoundException, SQLException {
		getDbParams().setParams(host, user, pass, base, port);
		_connect();
	}

	public void connect(String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException {
		getDbParams().setParams(host, user, pass, base, port, options);
		_connect();
	}

	public void connect() throws ClassNotFoundException, SQLException {
		connect(params.getHost(), params.getUser(), params.getPass(), params.getBase(), params.getPort(), params.getOptions());
	}

	public ArrayList<String> getDbNames() throws SQLException {
		ResultSet rs = connection.getMetaData().getCatalogs();
		ArrayList<String> ret = new ArrayList<String>();
		while (rs.next())
			ret.add(rs.getString("TABLE_CAT"));
		return ret;
	}

	public ArrayList<String> getTableNames(String dbName) throws SQLException {
		ResultSet tables = connection.getMetaData().getTables(dbName, null, "%", null);
		ArrayList<String> ret = new ArrayList<String>();
		while (tables.next())
			ret.add(tables.getString("TABLE_NAME"));
		return ret;
	}

	public ArrayList<String> getFieldNames(String tableName) throws SQLException {
		ResultSet rs = connection.getMetaData().getColumns(connection.getCatalog(), null, tableName, "%");
		ArrayList<String> ret = new ArrayList<String>();
		while (rs.next()) {
			ret.add(rs.getString("COLUMN_NAME"));
		}
		return ret;
	}

	public String autoInc(String tableName) throws SQLException {
		String autoIncField = "";
		ResultSet rs = getStatement().executeQuery(KSqlQuery.makeSelect(tableName));
		ResultSetMetaData rsmd = rs.getMetaData();
		int i = 1;
		while (i <= rsmd.getColumnCount() && autoIncField.equals("")) {
			if (rsmd.isAutoIncrement(i))
				autoIncField = rsmd.getColumnName(i);
			i++;
		}
		return autoIncField;
	}

	public Map<String, KDbField> getFieldNamesAndTypes(String tableName) throws SQLException {
		Map<String, KDbField> ret = new TreeMap<String, KDbField>(String.CASE_INSENSITIVE_ORDER);
		ResultSet rs = null;
		try {
			rs = getStatement().executeQuery(KSqlQuery.makeSelect(QUOTE() + tableName + QUOTE()));
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++)
				ret.put(rsmd.getColumnName(i), new KDbField(i, rsmd));

			Set<String> keys = getKeyFields(tableName);
			for (String pkKield : keys) {
				if (ret.containsKey(pkKield))
					((KDbField) ret.get(pkKield)).setPrimary(true);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return ret;
	}

	public Set<String> getKeyFields(String tableName) throws SQLException {
		ResultSet clefs = null;
		Set<String> ret = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		try {
			clefs = connection.getMetaData().getIndexInfo(null, null, tableName, true, true);
			while (clefs.next()) {
				String str = clefs.getString("INDEX_NAME");
				if (str != null)
					if (str.toLowerCase().startsWith("pri"))
						ret.add(clefs.getString("COLUMN_NAME"));
			}
		} finally {
			if (clefs != null)
				clefs.close();
		}
		return ret;
	}

	public List<KUniqueConstraint> getUniqueIndexes(String tableName) throws SQLException {
		ResultSet rs = null;
		List<KUniqueConstraint> result = new ArrayList<>();
		try {
			rs = connection.getMetaData().getIndexInfo(null, null, tableName, true, true);
			boolean primary = false;
			KUniqueConstraint constraintUniqueList = null;
			while (rs.next()) {
				String indexName = rs.getString("INDEX_NAME");
				if ((indexName != null && !"".equals(indexName))) {
					if (indexName.toLowerCase().startsWith("pri"))
						primary = true;
					else {
						if (constraintUniqueList == null || !constraintUniqueList.getName().equals(indexName)) {
							constraintUniqueList = new KUniqueConstraint(indexName);
							result.add(constraintUniqueList);
						}
						primary = false;
					}
				}
				if (!primary)
					constraintUniqueList.addField(rs.getString("COLUMN_NAME"));
			}
		} finally {
			if (rs != null)
				rs.close();
		}

		return result;
	}

	public KDBForeignKeyList getForeignKeys(String tableName) throws SQLException {
		ResultSet keys = null;
		KDBForeignKeyList ret = new KDBForeignKeyList();
		try {
			keys = connection.getMetaData().getExportedKeys(null, null, tableName);
			while (keys.next()) {
				KDBForeignKey fk = new KDBForeignKey(keys.getString("FK_NAME"), keys.getString("PKTABLE_NAME"), keys.getString("PKCOLUMN_NAME"), keys.getString("FKTABLE_NAME"), keys.getString("FKCOLUMN_NAME"));
				short update = keys.getShort("UPDATE_RULE");
				short delete = keys.getShort("DELETE_RULE");
				fk.setDeleteRule(KDBForeignKeyDef.getActionFromDb(delete));
				fk.setUpdateRule(KDBForeignKeyDef.getActionFromDb(update));
				ret.add(fk);
			}
		} finally {
			if (keys != null)
				keys.close();
		}
		return ret;
	}

	public boolean tableExist(String tableName) {
		boolean ret = false;
		String query = "SELECT * FROM  " + QUOTE() + tableName + QUOTE() + " WHERE 1=0";
		ResultSet rs = null;
		try {
			Statement stmt = getStatement();
			rs = stmt.executeQuery(query);
			ret = true;
		} catch (Exception e) {
			ret = false;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
		}
		return ret;
	}

	public static Connection getConnexion(String host, String user, String pass, String base) {
		return null;
	}

	public KDbResultSet sendQuery(String sql) throws SQLException {
		KDebugConsole.print(sql, "SQL", "KDataBase.sendQuery");
		ResultSet resultset = getStatement().executeQuery(sql);
		result = resultset;
		return new KDbResultSet(resultset, this);
	}

	public Object getValue(String sql) throws SQLException {
		Object o = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = getStatement(true);
			rs = st.executeQuery(sql);
			// result = resultset;
			if (rs.next()) {
				o = rs.getObject(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return o;
	}

	private ArrayList<String> getInsert_id() throws SQLException {
		ResultSet resultSet = getStatement().getGeneratedKeys();
		ArrayList<String> result = new ArrayList<String>();
		if (resultSet != null && resultSet.next()) {
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			int columnCount = rsMetaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				if (rsMetaData.isAutoIncrement(i) || columnCount == 1)
					result.add(resultSet.getString(i));
			}
		}
		return result;
	}

	public ArrayList<String> getGeneratedKeys() {
		return generatedKeys;
	}

	public int num_rows() throws SQLException {
		int res = 0;
		if (result != null) {
			do {
			} while (result.next());
			res = result.getRow();
		}
		return res;

	}

	public int rowCount(String sql) throws SQLException {
		int result = 0;
		sql = KSqlQuery.getSelectWithNewFields(sql, "count(*)");
		ResultSet rs = null;
		try {
			rs = getStatement().executeQuery(sql);
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return result;
	}

	public KDbParams getParams() {
		return params;
	}

	public String getBase() {
		if (connection != null)
			try {
				return connection.getCatalog();
			} catch (SQLException e) {
			}
		return "";
	}

	public void close() throws SQLException {
		if (pool != null) {
			pool.returnConnection(this);
			KDebugConsole.print(this + " dispo", "POOL", "KDataBase.close");
		} else if (connection != null) {
			if (statement != null)
				statement.close();
			connection.close();
		}
	}

	public int execute(String sql) throws SQLException {
		int result = -1;
		if (sql != null && !"".equals(sql)) {
			KDebugConsole.print(sql, "SQL", "KDataBase.execute");
			int areGeneratedKeys = Statement.NO_GENERATED_KEYS;
			if (sql.toLowerCase().startsWith("insert"))
				areGeneratedKeys = Statement.RETURN_GENERATED_KEYS;
			result = getStatement().executeUpdate(sql, areGeneratedKeys);
			if (areGeneratedKeys == Statement.RETURN_GENERATED_KEYS) {
				generatedKeys = getInsert_id();
			}
		}
		return result;
	}

	public int[] executeBatch(String sql) throws SQLException {
		int[] result = new int[] {};
		if (sql != null && !"".equals(sql)) {
			String[] batchs = sql.split(";");
			Statement st = getStatement();

			if (connection.getMetaData().supportsBatchUpdates()) {
				connection.setAutoCommit(false);
				st.clearBatch();
				for (String b : batchs) {
					b = b.trim();
					if (!"".equals(b)) {
						KDebugConsole.print(b, "SQL", "KDataBase.executeBatch");
						st.addBatch(b.trim());
					}
				}
				result = st.executeBatch();
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		return result;
	}

	public Map<String, Object> fetchMap() throws SQLException {
		if (result.next()) {
			return new KDbResultSet(result, this).toMap();
		}
		return null;
	}

	public String getOptions() {
		String result = "";
		if (params != null)
			result = params.getOptions();
		return result;
	}

	public void setOptions(String options) {
		if (params != null)
			this.params.setOptions(options);
	}

	public String getBaseName() {
		return params.getBase();
	}

	public synchronized boolean lease() {
		if (inuse) {
			return false;
		} else {
			inuse = true;
			KDebugConsole.print(this + " inuse", "POOL", "KDataBase.lease");
			timestamp = System.currentTimeMillis();
			return true;
		}
	}

	public boolean inUse() {
		return inuse;
	}

	public long getLastUse() {
		return timestamp;
	}

	public void expireLease() {
		inuse = false;
		KDebugConsole.print(this + " expirelease", "POOL", "KDataBase.expireLease");
	}

	public void setPool(KDbConnectionPool pool) {
		this.pool = pool;
	}

	public String getURL() {
		return getURL(params);
	}

	public abstract String getURL(KDbParams params);

	public void clear() {
		try {
			if (result != null)
				result.close();
			if (statement != null)
				statement.close();
			free();
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
		}
	}

	public String PCHAR() {
		return this.dbSpec.getProtectChar();
	}

	public String AUTOINC() {
		return this.dbSpec.getAutoIncMask();
	}

	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "BIT";
		if (aClazz.equals(Integer.class) || aClazz.equals(int.class))
			return "INTEGER";
		if (aClazz.equals(Long.class) || aClazz.equals(long.class))
			return "BIGINT";
		if (aClazz.equals(Short.class) || aClazz.equals(short.class))
			return "SMALLINT";
		if (aClazz.equals(Double.class) || aClazz.equals(double.class))
			return "DOUBLE";
		if (aClazz.equals(Float.class) || aClazz.equals(float.class))
			return "REAL";
		if (aClazz.equals(Byte.class) || aClazz.equals(byte.class))
			return "TINYINT";
		if (aClazz.equals(Date.class))
			return "DATE";
		if (aClazz.equals(java.sql.Date.class))
			return "DATE";
		if (aClazz.equals(java.sql.Time.class))
			return "TIME";
		if (aClazz.equals(String.class))
			return "VARCHAR(50)";
		if (aClazz.equals(char.class) || aClazz.equals(Character.class))
			return "VARCHAR(1)";
		return null;
	}

	public String javaTypeToDbType(KMetaField metaField) {
		Class<?> fieldType = metaField.getField().getType();
		String result = metaField.getFieldType();
		if ("".equals(result)) {
			result = javaTypeToDbType(fieldType);
		}
		if (result != null) {
			if (String.class.isAssignableFrom(fieldType)) {
				String precision = metaField.getStringSize().toString();
				if (!"".equals(precision))
					result = KString.getBefore(result, "(") + precision;
			}
			if (Number.class.isAssignableFrom(Converter.getWrapperClass(fieldType))) {
				String precision = metaField.getDecimalSize().toString();
				if (!"".equals(precision))
					result = KString.getBefore(result, "(") + precision;
			}
		}
		return result;
	}

	public Class<?> toJavaClass(String dbType, Class<?> _default) {
		dbType = dbType.toLowerCase();
		if (dbType.equalsIgnoreCase("bit") || dbType.toLowerCase().contains("bool") || dbType.equalsIgnoreCase("tinyint"))
			return boolean.class;
		if (dbType.equalsIgnoreCase("bigint"))
			return long.class;
		if (dbType.toLowerCase().contains("smallint") || dbType.contains("tinyint") || dbType.contains("mediumint"))
			return int.class;
		if (dbType.equalsIgnoreCase("integer") || dbType.contains("int"))
			return int.class;
		if (dbType.equalsIgnoreCase("double"))
			return double.class;
		if (dbType.toLowerCase().contains("float") || dbType.equalsIgnoreCase("real") || dbType.contains("decimal"))
			return float.class;
		if (dbType.equalsIgnoreCase("date") || dbType.equalsIgnoreCase("datetime"))
			return java.sql.Date.class;
		if (dbType.equalsIgnoreCase("time"))
			return java.sql.Time.class;
		if (dbType.toLowerCase().contains("timestamp"))
			return java.sql.Timestamp.class;
		if (dbType.equalsIgnoreCase("year"))
			return int.class;
		if (dbType.contains("enum") || dbType.contains("set"))
			return String.class;
		if (dbType.equalsIgnoreCase("blob"))
			return String.class;
		if (dbType.equalsIgnoreCase("text") || dbType.contains("char") || dbType.equalsIgnoreCase("enum"))
			return String.class;

		return _default;
	}

	public KDBSpec getDbSpec() {
		return dbSpec;
	}

	public String QUOTE() {
		if (dbSpec == null)
			return Ko.kQuoteChar();
		return dbSpec.getProtectChar();
	}

	public String limit(int limit, int offset) {
		if (dbSpec == null)
			return " LIMIT " + limit + "," + offset;
		return dbSpec.limit(limit, offset);
	}

	public void setParams(KDbParams params) {
		this.params = params;
	}

	public void beginTrans() throws SQLException {
		if (isSupportsTransactions())
			connection.setAutoCommit(false);
	}

	public void rollBackTrans() throws SQLException {
		if (supportsTransactions) {
			connection.rollback();
			connection.setAutoCommit(true);
		}
	}

	public void commitTrans() throws SQLException {
		if (supportsTransactions) {
			connection.commit();
			connection.setAutoCommit(true);
		}
	}

	public boolean isSupportsTransactions() throws SQLException {
		boolean result = supportsTransactions;
		if (result)
			return true;
		if (connection != null)
			result = connection.getMetaData().supportsTransactions();
		supportsTransactions = result;
		return supportsTransactions;
	}

	public String getFkName(String tableName, String fkField, String referencesTable, String referencesField) {
		return dbSpec.getFkName(tableName, fkField, referencesTable, referencesField);
	}

	public int executeUpdate(PreparedStatement st) throws SQLException {
		return st.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends KDataBase> getDbClassInstance(String koDbType) {

		String dbClassName = "net.ko.db.provider.";
		if (koDbType == null || "".equalsIgnoreCase(koDbType)) {
			return null;
		}
		koDbType = koDbType.toLowerCase();
		dbClassName += "KDB" + KString.capitalizeFirstLetter(koDbType);

		Class<? extends KDataBase> c = null;
		try {
			c = (Class<? extends KDataBase>) Class.forName(dbClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
		return c;
	}

	public Connection createDatabase(String nomBase) throws SQLException {
		Connection tempConn = null;
		Statement st = null;
		try {
			params.setBase("");
			String u = getURL();
			tempConn = DriverManager.getConnection(u, params.getUser(), params.getPass());
			st = tempConn.createStatement();
			st.execute(dbSpec.createDataBase(QUOTE() + nomBase + QUOTE()));
			params.setBase(nomBase);
			u = getURL();
			connection = DriverManager.getConnection(u, params.getUser(), params.getPass());
		} catch (SQLException e) {
			SQLException sqle = new SQLException("Création de la base impossible");
			sqle.setNextException(e);
			throw sqle;
		} finally {
			try {
				if (st != null)
					st.close();
				if (tempConn != null)
					tempConn.close();
			} catch (Exception e) {
			}
		}
		return connection;
	}

	private KDbParams getDbParams() {
		if (params == null)
			params = new KDbParams();
		return params;
	}

	public boolean implementsCreateDb() {
		return false;
	}

	public int free() throws SQLException {
		return -1;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}
}
