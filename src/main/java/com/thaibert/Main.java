package com.thaibert;

import java.sql.SQLException;
import java.util.*;

class Main {
  // Args passed in as e.g. -Ddir=<dir>
  static final String ACTION = "action";
  static final String DIR = "dir";
  static final String FILE_TYPES = "filetypes";
  static final String JDBC_CONNECTION = "jdbc";
  static final String TABLE_NAME = "table";


  public static void main(String[] args) {
    validateArgs();

    String action = System.getProperty(ACTION);
    String dir = System.getProperty(DIR);

    Collection<String> wantedFileTypes = Arrays.asList(
      System.getProperty(FILE_TYPES, ",").split(",")
    );

    String jdbcUrl = System.getProperty(JDBC_CONNECTION);
    String table = System.getProperty(TABLE_NAME);

    try {
      DatabaseClient db = new DatabaseClient(jdbcUrl, table);

      switch (action) {
        case "watch": {
          Collection<Inode> inodes = FileUtil.filesIn(dir, wantedFileTypes);
          inodes.forEach(x -> db.insert(x.inode(), x.relativizedPath()));
          break;
        }
        default: {
          throw new IllegalArgumentException("Unknown action \"" + action + "\"");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void validateArgs() {
    final List<String> requiredArgs = Arrays.asList(
      ACTION,
      DIR,
      JDBC_CONNECTION,
      TABLE_NAME
    );
    final List<String> optionalArgs = Arrays.asList(
      FILE_TYPES
    );

    requiredArgs.forEach(arg -> {
      if (System.getProperty(arg) == null) {
        System.err.println("Argument -D" + arg + "=<...> is not set");

        System.err.println("Required args:");
        requiredArgs.forEach(x -> System.err.println("\t-D" + x + "=<...>"));
        System.err.println("Optional args:");
        optionalArgs.forEach(x -> System.err.println("\t-D" + x + "=<...>"));

        System.exit(1);
      }
    });
  }
}
