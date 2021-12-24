package ru.alexus.twitchbot.twitch;

import org.jetbrains.annotations.NotNull;
import ru.alexus.twitchbot.Globals;

import java.sql.*;

public class Database {

	static class UpdateValue {
		String name;
		Object value;

		public UpdateValue(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	private final String address;
	private final String dbname;
	private final String login;
	private final String pass;
	private Connection dbConnection;

	public Database(String address, String dbname, String login, String pass) {

		if (address.endsWith("/")) this.address = address;
		else this.address = address + "/";
		this.dbname = dbname;
		this.login = login;
		this.pass = pass;
	}

	public void connect() throws SQLException {
		DriverManager.setLoginTimeout(5);
		dbConnection = DriverManager.getConnection(address + dbname, login, pass);
	}

	public void disconnect() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbConnection = null;
	}

	public boolean isConnected() {
		return dbConnection != null;
	}

	public void executeInsert(String table, String fields, String valuesScheme, Object @NotNull ... values) throws SQLException {
		String sql = "INSERT INTO " + table + " (" + fields + ") VALUES (" + valuesScheme + ")";
		PreparedStatement statement = dbConnection.prepareStatement(sql);
		for (int i = 0; i < values.length; i++) {
			statement.setObject(i + 1, values[i]);
		}
		statement.execute();
		statement.close();
	}

	public void executeUpdate(String table, String setScheme, String whereScheme, Object @NotNull ... values) throws SQLException {
		String sql = "UPDATE " + table + " SET " + setScheme + " WHERE " + whereScheme;
		PreparedStatement statement = dbConnection.prepareStatement(sql);
		for (int i = 0; i < values.length; i++) {
			statement.setObject(i + 1, values[i]);
		}
		statement.execute();
	}

	public ResultSet execute(String sql, Object... values) {
		try {
			PreparedStatement statement = dbConnection.prepareStatement(sql);
			for (int i = 0; i < values.length; i++) {
				statement.setObject(i + 1, values[i]);
			}
			if (sql.startsWith("SELECT"))
				return statement.executeQuery();
			else {
				statement.execute();
				return null;
			}
		} catch (Exception e) {
			Globals.log.error("Failed to execute query");
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet executeSelect(String table) {
		return executeSelect(table, null);
	}

	public ResultSet executeSelect(String table, String fields) {
		return executeSelect(table, fields, null);
	}

	public ResultSet executeSelect(String table, String fields, String whereScheme, Object... whereValues) {
		String sql = "SELECT ";
		if (fields != null) sql += fields;
		else sql += "*";
		sql += " FROM " + table;
		if (whereScheme != null && whereValues != null) sql += " WHERE " + whereScheme;

		try {
			PreparedStatement statement = dbConnection.prepareStatement(sql);
			if (whereScheme != null && whereValues != null) {
				for (int i = 0; i < whereValues.length; i++) {
					statement.setObject(i + 1, whereValues[i]);
				}
			}
			return statement.executeQuery();
		} catch (Exception e) {
			return null;
		}
	}

	/*public void executeUpdateMultiple(String table, String where, UpdateFieldInfo... infos) throws SQLException {
	/*
	UPDATE table SET Col1 = CASE id
                  WHEN 1 THEN 1
                  WHEN 2 THEN 2
                  WHEN 4 THEN 10
                  ELSE Col1
                END
     WHERE id IN (1, 2, 3, 4);
	 */
	/*	StringBuilder mutableValues = new StringBuilder();
		StringBuilder whereStr1 = new StringBuilder();
		coinsValues.append("CASE").append("\n");
		mutableValues.append("CASE").append("\n");
		whereStr1.append("(");
		String delim = "";
		for (UpdateFieldInfo updateValues : infos) {

			StringBuilder valuesBuilder = new StringBuilder();
			valuesBuilder.append("CASE").append("\n");
			for (UpdateValue value : updateValues.values){
				valuesBuilder.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.getBuggycoins()).append("\n");
			}
			mutableValues.append("WHEN users.twitchID = ").append(user.getUserId()).append(" THEN ").append(user.isMutableByOthers() ? 1 : 0).append("\n");

			whereStr1.append(delim).append(user.getUserId());
			delim = ",";
		}
		whereStr1.append(")");
		coinsValues.append("ELSE buggycoins").append("\n END");
		mutableValues.append("ELSE mutable").append("\n END");
		String sql = "UPDATE "+table+" SET buggycoins = " + coinsValues + ", mutable = " + mutableValues + " WHERE "+where+" IN " + whereStr1;
		//System.out.println(sql);
		PreparedStatement statement = dbConnection.prepareStatement(sql);
		statement.execute();
	}*/

}
