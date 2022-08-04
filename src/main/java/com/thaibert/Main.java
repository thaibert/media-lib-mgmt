package com.thaibert;

import java.sql.SQLException;
import java.util.*;

class Main {
  public static void main(String[] args) throws Exception {
    // ... TODO: arg parsing etc.
    String rootDir = "";
    Collection<String> wantedFileTypes = Arrays.asList("");

    String url = "";
    String table = "";

    try {
      DatabaseClient db = new DatabaseClient(url, table);

      Collection<Inode> inodes = FileUtil.filesIn(rootDir, wantedFileTypes);

      inodes.forEach(x -> db.insert(x.inode(), x.relativizedPath()));
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
