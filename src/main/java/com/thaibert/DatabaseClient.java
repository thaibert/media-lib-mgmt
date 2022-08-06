package com.thaibert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DatabaseClient {

  private String table;
  private Connection db;

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
            "NULL" +
          ")" +
          " ON CONFLICT DO NOTHING;");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Map<String, String> getFilesToLink() {
    try (Statement st = db.createStatement()) {
      ResultSet rs = st.executeQuery(
        "SELECT * FROM " + table + " WHERE clean_name IS NOT NULL"
      );

      Map<String, String> links = new HashMap<>();
      while (rs.next()) {
        String dirty = rs.getString("raw_name");
        String clean = rs.getString("clean_name");
        links.put(dirty, clean);
      }
      return links;
    } catch(SQLException e) {
      e.printStackTrace();
      return Collections.emptyMap();
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
