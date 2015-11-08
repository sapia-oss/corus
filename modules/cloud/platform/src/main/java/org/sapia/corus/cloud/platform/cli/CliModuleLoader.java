package org.sapia.corus.cloud.platform.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

/**
 * This class acts as a central-entry point for retrieving module instances at runtime.
 * 
 * @author yduchesne
 *
 */
public class CliModuleLoader {
  
  private static final String BASE_RESOURCE_PATH = "org.sapia.corus.cloud.platform.cli".replace(".", "/");
  
  private static CliModuleLoader INSTANCE = new CliModuleLoader();
  
  private Map<String, CliModule> moduleCache = new ConcurrentHashMap<String, CliModule>();

  /**
   * Package-visibility for testing.
   */
  CliModuleLoader() {
  }
  
  /**
   * @return the {@link CliModuleLoader} singleton.
   */
  public static CliModuleLoader getInstance() {
    return INSTANCE;
  }

  /**
   * @param provider the provider to load module for.
   * @param commandName the name of the command whose corresponding module should be loaded.
   * @return the {@link CliModule} instance corresponding to the given provider and command name.
   */
  public CliModule load(String provider, String commandName) {
    String moduleKey = provider + "." + commandName;
    CliModule toReturn = moduleCache.get(moduleKey);
    if (toReturn != null) {
      return toReturn;
    }
    return doLoad(moduleKey, provider, commandName);
  }
  
  private synchronized CliModule doLoad(String moduleKey, String provider, String commandName) {
    CliModule toReturn = moduleCache.get(moduleKey);
    if (toReturn != null) {
      return toReturn;
    }
    
    String      providerConfPath   = BASE_RESOURCE_PATH + "/" + provider + ".properties";
    Properties  props              = new Properties();
    InputStream providerConfStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(providerConfPath);
    String      moduleClassName    = null;

    Preconditions.checkState(providerConfStream != null, "Invalid config path (could not find resource): " + providerConfPath);
    
    try {
      props.load(providerConfStream);
      moduleClassName = props.getProperty(commandName);
      
      Preconditions.checkState(moduleClassName != null, "'" + commandName + "' command not implemented by provider: " + provider);
      
      toReturn = (CliModule) Thread.currentThread().getContextClassLoader().loadClass(moduleClassName).newInstance();
      moduleCache.put(moduleKey, toReturn);
      return toReturn;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Invalid module class: " + moduleClassName);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Illegal access to module class: " + moduleClassName 
          + ". Make sure class has public no-args constructor");
    } catch (InstantiationException e) {
      throw new IllegalStateException("Could not create module instance for class: " + moduleClassName 
          + ". Make sure class has public no-args constructor");
    } catch (IOException ioe) {
     throw new IllegalStateException("I/O error occurred while loading module provider config: " + providerConfPath, ioe);
    
    } finally {
      try {
        providerConfStream.close();
      } catch (IOException e) {
        // ignore
      }
    }
    
  }
  
}
