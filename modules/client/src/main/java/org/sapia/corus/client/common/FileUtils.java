package org.sapia.corus.client.common;


/**
 * Hold various file-related utility methods.
 * 
 * @author yduchesne
 *
 */
public class FileUtils {
  
  
  public static class FileInfo{
    public String  directory;
    public String  fileName;
    public boolean isClasses;
  }
  
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  
  public static final String PATH_SEPARATOR = System.getProperty("path.separator");

  /**
   * @param dir a {@link String} corresponding to the path to a file or directory.
   * @return
   */
  public static boolean isAbsolute(String path){
    return path.startsWith("/") || isWindowsDrive(path); 
  }
  
  /**
   * @param c a <code>char</code> to test.
   * @return <code>true</code> if the given character corresponds to a path separator.
   */
  public static boolean isPathSeparator(char c){
    return c == '/' || c == '\\';
  }
 
  /**
   * @param path a {@link String} corresponding to the path to a file or directory.
   * @return <code>true</code> if the path starts with a Windows drive identifier (e.g.: <code>C:</code>).
   */
  public static boolean isWindowsDrive(String path){
    return path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':'; 
  }
  
  /**
   * @param directory the path for which to obtain the corresponding {@link FileInfo}. 
   * @return a {@link FileInfo}.
   */
  public static FileInfo getFileInfo(String path){
    String trimmedPath = path.trim();
    int separatorIndex = trimmedPath.lastIndexOf("/");
    FileInfo info = new FileInfo();
    if(trimmedPath.endsWith("/")){
      info.directory = path;
      info.isClasses = true;
    }
    else if(separatorIndex > 1){
      String fileName = path.substring(separatorIndex+1);
      if(fileName.contains(".")){
        info.directory = path.substring(0, separatorIndex);
        info.fileName  = fileName;
      }
      else{
        info.directory = path;
      }
    }
    else{
      info.directory = path;
    }
    return info;
  }
}

