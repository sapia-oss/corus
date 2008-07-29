/*
 * NullImplementation.java
 *
 * Created on October 26, 2005, 9:57 AM
 */

package org.sapia.corus.interop.api;

/**
 *
 * @author yduchesne
 */
public class NullImplementation implements Implementation{
  
  public static final int UNDEFINED_PORT = -1;  
  
  /** Creates a new instance of NullImplementation */
  public NullImplementation() {
  }
  
  public String getCorusPid() {
    return System.getProperty(Consts.CORUS_PID);
  }

  public String getDistributionName() {
    return System.getProperty(Consts.CORUS_DIST_NAME);
  }

  public String getDistributionVersion() {
    return System.getProperty(Consts.CORUS_DIST_VERSION);
  }

  public String getDistributionDir() {
    return System.getProperty(Consts.CORUS_DIST_DIR);
  }

  public String getCorusHost() {
    return System.getProperty(Consts.CORUS_SERVER_HOST);
  }

  public int getCorusPort() {
    if (System.getProperty(Consts.CORUS_SERVER_PORT) != null) {
      return Integer.parseInt(System.getProperty(Consts.CORUS_SERVER_PORT));
    }

    return UNDEFINED_PORT;
  }

  public boolean isDynamic() {
    return false;
  }

  public void restart(){}

  public void shutdown() {}

  public void addShutdownListener(ShutdownListener listener){}
  
  public synchronized void addStatusRequestListener(StatusRequestListener listener) {}  
  
}
