package org.sapia.corus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusRuntime {
  
  static final String CORUS_PROCESS_FILE = "corus_process";
  
  static CorusImpl _instance;
  static CorusTransport _transport;
  static String _baseDir;
  static String _corusHome;

  public static Corus getCorus() throws IllegalStateException {
    if (_instance == null) {
      throw new IllegalStateException("Corus not initialized");
    }

    return _instance;
  }

  public static CorusTransport getTransport() throws IllegalStateException {
    if (_transport == null) {
      throw new IllegalStateException("Corus Transport not initialized");
    }

    return _transport;
  }

  public static String getCorusHome() {
    return _corusHome;
  }
  
  /**
   * This method returns properties that can be defined for all processes managed
   * by all Corus servers on this host, or for processes that are part of a 
   * given domain on this host.
   * <p>
   * Properties must be specified in Java property files under the Corus home
   * directory. For multi-domain properties, a file named 
   * <code>corus_process.properties</code> is searched. For domain-specific
   * properties, a file named <code>corus_process_someDomain.properties</code>
   * is searched.
   * <p>
   * Domain properties override global (multi-domain) properties.
   * <p>
   * The properties are passed to the processes upon their startup.
   */
  public static Properties getProcessProperties() throws IOException{
    File home = new File(_corusHome + File.separator + "config");
    File globalProps = new File(home, CORUS_PROCESS_FILE + ".properties");
    Properties globals = new Properties();
    if(globalProps.exists()){
      FileInputStream stream = new FileInputStream(globalProps);
      try{
        globals.load(stream);
      }finally{
        stream.close();
      }
    }
    File domainProps = new File(home, CORUS_PROCESS_FILE + "_" + 
      _instance.getDomain() + ".properties");
    if(domainProps.exists()){
      FileInputStream stream = new FileInputStream(domainProps);
      try{
        globals.load(stream);
      }finally{
        stream.close();
      }
    }    
    return globals;
  }

  static void init(CorusImpl dyn, String corusHome, CorusTransport aTransport) {
    _instance   = dyn;
    _corusHome = corusHome;
    _transport = aTransport;
  }
  
  static void shutdown() {
    _transport.shutdown();
    CorusImpl.shutdown();
  }
}
