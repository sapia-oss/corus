package org.sapia.corus.client.services.cluster;

import java.io.Serializable;

import org.sapia.ubik.net.ServerAddress;

/**
 * 
 * @author J-C Desrochers
 */
public class ServerHost implements Serializable {
  
  static final long serialVersionUID = 1L;

  private ServerAddress _serverAddress;
  private String _osInfo;
  private String _javaVmInfo;

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
    return _serverAddress;
  }

  /**
   * Changes the value of the attributes serverAddress.
   *
   * @param aServerAddress The new value of the serverAddress attribute.
   */
  public void setServerAddress(ServerAddress aServerAddress) {
    _serverAddress = aServerAddress;
  }

  /**
   * Returns the osInfo attribute.
   *
   * @return The osInfo value.
   */
  public String getOsInfo() {
    return _osInfo;
  }

  /**
   * Changes the value of the attributes osInfo.
   *
   * @param aOsInfo The new value of the osInfo attribute.
   */
  public void setOsInfo(String aOsInfo) {
    _osInfo = aOsInfo;
  }

  /**
   * Returns the javaVmInfo attribute.
   *
   * @return The javaVmInfo value.
   */
  public String getJavaVmInfo() {
    return _javaVmInfo;
  }

  /**
   * Changes the value of the attributes javaVmInfo.
   *
   * @param aJavaVmInfo The new value of the javaVmInfo attribute.
   */
  public void setJavaVmInfo(String aJavaVmInfo) {
    _javaVmInfo = aJavaVmInfo;
  }
  
  @Override
  public int hashCode() {
    return _serverAddress.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof ServerHost){
      ServerHost host = (ServerHost)obj;
      return _serverAddress.equals(host.getServerAddress());
    }
    return false;
   
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new StringBuffer().append("[").
            append("address=").append(String.valueOf(_serverAddress)).
            append(" osInfo=").append(_osInfo).
            append(" javaVmInfo=").append(_javaVmInfo).
            append("]").toString();
  }
  
}
