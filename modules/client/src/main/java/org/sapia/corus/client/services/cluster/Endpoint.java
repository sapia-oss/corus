package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class holds the addresses of a Corus server, namely:
 * 
 * <ul>
 * <li>The unicast address of the event channel that the Corus server uses to
 * receive/send notifications across the cluster.
 * <li>The server's address (to which the server is bound, and through which it
 * receives requests).
 * </ul>
 * 
 * @author yduchesne
 * 
 */
public class Endpoint implements Externalizable {

  private ServerAddress serverAddress;
  private ServerAddress channelAddress;

  /** Do not use: meant for externalization only. */
  public Endpoint() {
  }

  /**
   * @param serverAddress
   *          the address of the Corus server to which this instance
   *          corresponds.
   * @param channelAddress
   *          the unicast adress of the Corus server to which this instance
   *          corresponds.
   */
  public Endpoint(ServerAddress serverAddress, ServerAddress channelAddress) {
    this.serverAddress = serverAddress;
    this.channelAddress = channelAddress;
  }

  /**
   * @return the unicast adress of the Corus server to which this instance
   *         corresponds.
   */
  public ServerAddress getChannelAddress() {
    return channelAddress;
  }

  /**
   * @return the address of the Corus server to which this instance corresponds.
   */
  public ServerAddress getServerAddress() {
    return serverAddress;
  }

  /**
   * @return this instance's {@link ServerAddress}, cast as a {@link TCPAddress}
   *         .
   * 
   * @see #getServerAddress()
   */
  public TCPAddress getServerTcpAddress() {
    return (TCPAddress) serverAddress;
  }

  // --------------------------------------------------------------------------
  // java.lang.Object method overrides

  @Override
  public int hashCode() {
    return serverAddress.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Endpoint) {
      Endpoint other = (Endpoint) obj;
      return serverAddress.equals(other.serverAddress);
    }
    return false;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "server", serverAddress, "channel", channelAddress);
  }

  // --------------------------------------------------------------------------
  // Externalization

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.serverAddress = (ServerAddress) in.readObject();
    this.channelAddress = (ServerAddress) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(serverAddress);
    out.writeObject(channelAddress);
  }

}
