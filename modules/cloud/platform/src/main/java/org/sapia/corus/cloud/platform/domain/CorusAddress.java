package org.sapia.corus.cloud.platform.domain;

import java.util.Objects;

/**
 * Models the address (host:port) of a Corus host.
 * 
 * @author yduchesne
 *
 */
public class CorusAddress {

  private String host;
  private int    port;
  
  public CorusAddress(String host, int port) {
    this.host = host;
    this.port = port;
  }
  
  /**
   * @return the post of the Corus server to connect to.
   */
  public String getHost() {
    return host;
  }
  
  /**
   * @return the port of the Corus server to connect to.
   */
  public int getPort() {
    return port;
  }
  
  /**
   * @return the HTTP url corresponding to this instance.
   */
  public String asHttpUrl() {
    return "http://" + host + ":" + port;
  }

  /**
   * @return the HTTPS url corresponding to this instance.
   */
  public String asHttpsUrl() {
    return "https://" + host + ":" + port;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return new StringBuilder(host).append(':').append(port).toString();
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(host, port);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CorusAddress) {
      CorusAddress other = (CorusAddress) obj;
      return host.equals(other.host) && port == other.port;
    }
    return false;
  }
}
