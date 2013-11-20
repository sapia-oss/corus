package org.sapia.corus.client.common;

/**
 * Provides utilities for manipulating file paths.
 * 
 * @author yduchesne
 * 
 */
public class PathUtils {

  private PathUtils() {
  }

  /**
   * Appends <code>subPath</code> to <code>path</code>, adding a "/" character
   * in between as a separator, if required.
   * 
   * @param path
   *          the path to which to append a sub-path.
   * @param subPath
   *          the sub-path to append to the given path.
   * @return the path resulting from the concatenation.
   */
  public static final String append(String path, String subPath) {
    StringBuilder sb = new StringBuilder(path);
    append(sb, subPath);
    return sb.toString();
  }

  /**
   * Appends <code>subPath</code> to <code>path</code>, adding a "/" character
   * in between as a separator, if required.
   * 
   * @param path
   *          the path to which to append a sub-path.
   * @param subPath
   *          the sub-path to append to the given path.
   */
  public static final void append(StringBuilder path, String subPath) {
    if (subPath == null || subPath.length() == 0) {
      return;
    }
    if (subPath.startsWith("/") || subPath.startsWith("\\")) {
      if (subPath.length() == 1) {
        return;
      }
      subPath = subPath.substring(1);
    }
    if (path.length() > 0 && (path.charAt(path.length() - 1) == '/' || path.charAt(path.length() - 1) == '\\')) {
      path.append(subPath);
    } else {
      path.append("/").append(subPath);
    }
  }

  /**
   * Concatenates the given paths, putting a file separator in between each.
   * 
   * @param toConcat
   *          the paths to concatenate.
   * @return the concatenated path, with a separator in between.
   */
  public static final String toPath(String... toConcat) {
    StringBuilder sb = new StringBuilder();
    if (toConcat != null) {
      for (int i = 0; i < toConcat.length; i++) {
        if (i > 0) {
          sb.append("/");
        }
        sb.append(toConcat[i]);
      }
    }
    return sb.toString();
  }
}
