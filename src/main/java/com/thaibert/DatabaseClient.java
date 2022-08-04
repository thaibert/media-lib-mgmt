package com.thaibert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseClient {

  private String table;
  private Connection db;

  private static final String NULL_VALUE = "null";

  public DatabaseClient(String jdbcConnectionUrl, String table) throws SQLException {
    this.table = table;
    this.db = DriverManager.getConnection(jdbcConnectionUrl);

    initTable();
  }

  public void insert(String inode, String relativizedPath) {
    System.out.println("[INSERT] " + inode + ", " + relativizedPath);
    try (Statement st = db.createStatement()) {
      st.executeUpdate(
          "INSERT INTO " + table + " VALUES (" +
            quotes(inode) + ", " +
            quotes(relativizedPath) + ", " +
            NULL_VALUE +
          ")" +
          " ON CONFLICT DO NOTHING;");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void initTable() throws SQLException {
    try (Statement st = db.createStatement()) {
      st.executeUpdate(
          "CREATE TABLE IF NOT EXISTS " + this.table + "(" +
            "inode VARCHAR NOT NULL, " +
            "raw_name VARCHAR NOT NULL, " +
            "clean_name VARCHAR, " +
            "PRIMARY KEY ( inode )" +
          ");"
      );
    }
  }

  private static <T> String quotes(T x) {
    // Postgres specifically wants strings to have single quotes.
    return "'" + x.toString() + "'";
  }
}
