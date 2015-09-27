package net.ko.ksql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.ko.db.KDataBase;
import net.ko.db.KDbResultSet;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;

public class KPreparedStatement {
	private PreparedStatement statement;
	private KDataBase db;
	private String sql;
	private ArrayList<Object> generatedKeys;

	public KPreparedStatement(KDataBase db) {
		super();
		this.db = db;
	}

	public KPreparedStatement(KDataBase db, String sql) throws SQLException {
		super();
		this.db = db;
		setSql(sql);
	}

	public String getSql() {
		return sql;

	}

	public void setSql(String sql) throws SQLException {
		this.sql = sql;
		if (sql != null && !"".equals(sql)) {
			int areGeneratedKeys = PreparedStatement.NO_GENERATED_KEYS;
			if (sql.toLowerCase().startsWith("insert"))
				areGeneratedKeys = PreparedStatement.RETURN_GENERATED_KEYS;
			statement = db.getPreparedStatement(sql, areGeneratedKeys);
		}
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		statement.setObject(parameterIndex, x);
	}

	public void updateParameters(List<Object> values) {
		int index = 1;
		KDebugConsole.print(values + "", "SQL", "KPreparedStatement.updateParameters");
		for (Object o : values) {
			try {
				statement.setObject(index, o);
			} catch (SQLException e) {
				try {
					statement.setObject(index, null, Types.VARCHAR);
				} catch (SQLException e1) {
					Ko.klogger().log(Level.SEVERE, "Impossible d'affecter un paramètre de la requête", e1);
				}
			} finally {
				index++;
			}
		}
	}

	public int executeUpdate() throws SQLException {
		int result = -1;
		if (sql != null && !"".equals(sql)) {
			try {
				KDebugConsole.print(sql, "SQL", "KPreparedStatement.executeUpdate");
				result = db.executeUpdate(statement);

				if (sql.toLowerCase().startsWith("insert")) {
					generatedKeys = getInsert_id();
				}
			} finally {
				// if (statement != null)
				// statement.close();
			}
		}
		return result;
	}

	public int executeUpdate(KParameterizedExecute instruction) throws SQLException {
		String query = instruction.parse();
		setSql(query);
		updateParameters(instruction.getParameters());
		return executeUpdate();
	}

	private ArrayList<Object> getInsert_id() throws SQLException {
		ResultSet resultSet = statement.getGeneratedKeys();
		ArrayList<Object> result = new ArrayList<Object>();
		if (resultSet != null && resultSet.next()) {
			try {
				ResultSetMetaData rsMetaData = resultSet.getMetaData();
				int columnCount = rsMetaData.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					if (rsMetaData.isAutoIncrement(i) || columnCount == 1)
						result.add(resultSet.getString(i));
				}
			} finally {
				resultSet.close();
			}
		}

		return result;
	}

	public ArrayList<Object> getGeneratedKeys() {
		return generatedKeys;
	}

	public void close() throws SQLException {
		if (statement != null)
			statement.close();

	}

	public KDbResultSet sendQuery(KParameterizedInstruction instructionSql) throws SQLException {
		setSql(instructionSql.parse());
		return sendQuery(instructionSql.getParameters());
	}

	public KDbResultSet sendQuery(List<Object> parameters) throws SQLException {
		updateParameters(parameters);
		KDebugConsole.print(sql, "SQL", "KPreparedStatement.sendQuery");
		ResultSet resultset = statement.executeQuery();
		return new KDbResultSet(resultset, db);
	}
}
