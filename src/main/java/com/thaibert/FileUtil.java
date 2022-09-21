package com.thaibert;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

  private FileUtil() {
  }

  public static Collection<Inode> filesIn(String rootDir, Collection<String> desiredFileTypes) {
    Path root = Paths.get(rootDir);

    final Predicate<Path> isDirectory = (Path x) -> x.toFile().isDirectory();
    final Predicate<Path> isFile = isDirectory.negate();
    final Predicate<Path> isDesiredFileType = desiredFileTypes.isEmpty()
      ? (Path x) -> true 
      : (Path x) -> {
        String fileName = x.getFileName().toString();
        String fileEnding = fileName.substring(fileName.lastIndexOf(".") + 1);
        return desiredFileTypes.contains(fileEnding);
      };

    try (Stream<Path> files = Files.walk(root)) {
      return files.filter(isFile)
          .filter(isDesiredFileType)
          .map(x -> {
            try {
              // File keys are of the form: (dev=..., ino=...)
              String fileKey = Files.readAttributes(x, BasicFileAttributes.class)
                  .fileKey()
                  .toString();
              String[] fileKeys = fileKey
                  .replaceAll("[()]", "")
                  .split(",");
              String inode = Arrays.stream(fileKeys)
                  .map(s -> s.substring(s.indexOf("=") + 1))
                  .skip(1) // dev=..., ino=... ---> skip dev
                  .findFirst()
                  .get();
              return new Inode(inode, root.relativize(x).toString());
            } catch (IOException e) {
              e.printStackTrace();
              return null;
            }
          })
          .filter(x -> x != null)
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public static void link(String sourceFullyQualified, String targetFullyQualified) {
    try {
      Path link = Paths.get(targetFullyQualified).normalize();
      Path existing = Paths.get(sourceFullyQualified).normalize();

      Files.createDirectories(link.getParent());
      Files.createLink(link, existing);

      System.out.println("[LINKED] " + existing.toString() + "\t->\t" + link.toString());
    } catch (FileAlreadyExistsException e) {
      /* no-op at the moment. TODO: check if inode matches, otherwise overwrite with link to source. */
      System.out.println("         file already exists:\t" + targetFullyQualified);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
