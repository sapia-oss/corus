package org.sapia.corus.client.services.cluster;

import java.io.Serializable;

import org.sapia.ubik.net.ServerAddress;

/**
 * 
 * @author J-C Desrochers
 */
public class ServerHost implements Serializable {
  
  static final long serialVersionUID = 1L;

  private ServerAddress serverAddress;
  private String 				osInfo;
  private String 				javaVmInfo;

  public static ServerHost createNew(ServerAddress anAddress, String anOsInfo, String aJavaVmInfo) {
    ServerHost created = new ServerHost();
    created.setServerAddress(anAddress);
    created.setOsInfo(anOsInfo);
    created.setJavaVmInfo(aJavaVmInfo);
    
    return created;
  }
  
  /**
   * Creates a new {@link ServerHost} instance.
   */
  public ServerHost() {
  }

  /**
   * Returns the serverAddress attribute.
   *
   * @return The serverAddress value.
   */
  public ServerAddress getServerAddress() {
    return serverAddress;
  }

  /**
   * Changes the value of the attributes serverAddress.
   *
   * @param aServerAddress The new value of the serverAddress attribute.
   */
  public void setServerAddress(ServerAddress aServerAddress) {
    serverAddress = aServerAddress;
  }

  /**
   * Returns the osInfo attribute.
   *
   * @return The osInfo value.
   */
  public String getOsInfo() {
    return osInfo;
  }

  /**
   * Changes the value of the attributes osInfo.
   *
   * @param aOsInfo The new value of the osInfo attribute.
   */
  public void setOsInfo(String aOsInfo) {
    osInfo = aOsInfo;
  }

  /**
   * Returns the javaVmInfo attribute.
   *
   * @return The javaVmInfo value.
   */
  public String getJavaVmInfo() {
    return javaVmInfo;
  }

  /**
   * Changes the value of the attributes javaVmInfo.
   *
   * @param aJavaVmInfo The new value of the javaVmInfo attribute.
   */
  public void setJavaVmInfo(String aJavaVmInfo) {
    javaVmInfo = aJavaVmInfo;
  }
  
  @Override
  public int hashCode() {
    return serverAddress.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof ServerHost){
      ServerHost host = (ServerHost)obj;
      return serverAddress.equals(host.getServerAddress());
    }
    return false;
   
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new StringBuffer().append("[").
            append("address=").append(String.valueOf(serverAddress)).
            append(" osInfo=").append(osInfo).
            append(" javaVmInfo=").append(javaVmInfo).
            append("]").toString();
  }
  
}
