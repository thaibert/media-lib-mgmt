package com.thaibert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class Main {
  // Args passed in as e.g. -Ddir=<dir>
  static final String ACTION = "action";
  static final String SOURCE_DIR = "sourcedir";
  static final String TARGET_DIR = "targetdir";
  static final String FILE_TYPES = "filetypes";
  static final String JDBC_CONNECTION = "jdbc";
  static final String TABLE_NAME = "table";


  public static void main(String[] args) {
    validateArgs();

    String action = System.getProperty(ACTION);
    String sourceDir = System.getProperty(SOURCE_DIR);
    String targetDir = System.getProperty(TARGET_DIR);

    Collection<String> wantedFileTypes = Arrays.asList(
      System.getProperty(FILE_TYPES, ",").split(",")
    );

    String jdbcUrl = System.getProperty(JDBC_CONNECTION);
    String table = System.getProperty(TABLE_NAME);

    try {
      DatabaseClient db = new DatabaseClient(jdbcUrl, table);

      switch (action) {
        case "read": {
          System.out.println("Reading file system...");
          Collection<Inode> inodes = FileUtil.filesIn(sourceDir, wantedFileTypes);
          System.out.println("Found " + inodes.size() + " files.");

          inodes.forEach(x -> db.insert(x.inode(), x.relativizedPath()));
          break;
        }
        case "link": {
          System.out.println("Linking...");
          Map<String, String> filesToLink = db.getFilesToLink();
          System.out.println("Linking " + filesToLink.size() + " files.");

          filesToLink.forEach((source, target) -> FileUtil.link(
            sourceDir + "/" + source, 
            targetDir + "/" + target
          ));
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
      SOURCE_DIR,
      JDBC_CONNECTION,
      TABLE_NAME
    );
    final List<String> optionalArgs = Arrays.asList(
      FILE_TYPES,
      TARGET_DIR
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

    if ("link".equals(System.getProperty(ACTION))) {
      String targetDir = System.getProperty(TARGET_DIR);
      if (targetDir == null || targetDir == "") {
        System.err.println("Action \"link\" needs a target dir (-Dtargetdir=<...>).");
        System.exit(1);
      }
    }
  }
}
