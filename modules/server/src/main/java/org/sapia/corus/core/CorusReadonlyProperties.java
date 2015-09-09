package org.sapia.corus.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.util.Assertions;

/**
 * Class used to manage the .corus-readonly.properties file in a thread-safe manner.
 * 
 * @author yduchesne
 *
 */
public class CorusReadonlyProperties {

  public static final String BASE_NAME = ".corus-readonly";
  
  private CorusReadonlyProperties() {
  }
  
  /**
   * @param homeDir the {@link File} corresponding to the <tt>$HOME/.corus</tt> directory, from which the read-only properties are read.
   * @param port the port of the current Corus server.
   * @return return the read-only properties for the current server.
   */
  public static synchronized Properties load(File homeDir, int port) {
    File file = makeFile(homeDir, port);
    Properties props = new Properties();
    try {
      PropertiesUtil.loadIfExist(props, file);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load: " + file.getName(), e);
    }
    return props;
  }
  
  /**
   * @param target the {@link Properties} instance to which to load the read-only properties.
   * 
   * @param homeDir the {@link File} corresponding to the <tt>$HOME/.corus</tt> directory, from which the read-only properties are read.
   * @param port the port of the current Corus server.
   */
  public static synchronized void loadInto(Properties target, File homeDir, int port) {
    Properties toLoad = load(homeDir, port);
    PropertiesUtil.copy(toLoad, target);
  }
  
  /**
   * @param homeDir the {@link File} corresponding to the <tt>$HOME/.corus</tt> directory, to which the read-only properties are saved.
   * @param port the port of the current Corus server.
   * @param overwrite if <code>true</code>, overwrites any existing read-only Corus properties. Updates
   * the currently existing files (if any) otherwise.
   */
  public static synchronized void save(Properties props, File homeDir, int port, boolean overwrite) {
    Properties toWrite;
    
    if (!homeDir.exists()) {
      Assertions.illegalState(!homeDir.mkdirs(), "Could not create directory: " + homeDir.getAbsolutePath());
    }
    
    // updating: keeping the existing properties
    if (!overwrite) {
      toWrite = load(homeDir, port);
      for (String n : props.stringPropertyNames()) {
        String v = props.getProperty(n);
        if (v != null) {
          toWrite.setProperty(n, v);
        }
      }
    // overwriting
    } else {
      toWrite = props;
    }
    File toSave = makeFile(homeDir, port);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(toSave);
      toWrite.store(writer, "FILE WRITTEN BY CORUS SERVER. DO NOT MODIFY MANUALLY");
      writer.flush();
    } catch (IOException e) {
      throw new IllegalStateException("Could not save properties:" + toSave.getName(), e);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  private static File makeFile(File homeDir, int port) {
    return new File(homeDir, BASE_NAME + "-" + port + ".properties");
  }
  
}
