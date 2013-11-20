package org.sapia.corus.core;

import java.io.IOException;
import java.util.Properties;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.mcast.EventChannel;

/**
 * An instance of this class holds the state for a Corus server.
 * 
 * @author yduchesne
 * 
 */
public interface ServerContext {

  /**
   * @return this instance's {@link Corus}.
   */
  public Corus getCorus();

  /**
   * @return the name of the Corus server.
   */
  public String getServerName();

  /**
   * @param serverName
   *          a name to assign to this Corus server.
   */
  public void overrideServerName(String serverName);

  /**
   * @return the home directory of the Corus server.
   */
  public String getHomeDir();

  /**
   * @return the domain of the Corus server.
   */
  public String getDomain();

  /**
   * This method returns properties that can be defined for all processes
   * managed by all Corus servers on this host, or for processes that are part
   * of a given domain on this host.
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
  public Properties getProcessProperties() throws IOException;

  /**
   * @return the {@link CorusHost} instance corresponding to this Corus node.
   */
  public CorusHost getCorusHost();

  /**
   * @return the {@link CorusTransport}.
   */
  public CorusTransport getTransport();

  /**
   * @return the {@link InternalServiceContext} containing the services of the
   *         Corus server.
   */
  public InternalServiceContext getServices();

  /**
   * @return the {@link EventChannel} used by the Corus node corresponding to
   *         this instance.
   */
  public EventChannel getEventChannel();

  /**
   * Looks up a service of the given interface (internally delegates the call to
   * this instances {@link InternalServiceContext}.
   * 
   * @param <S>
   *          a service interface type
   * @param serviceInterface
   *          the service interface for which to find a service instance.
   * @return the service instance that was found.
   */
  public <S> S lookup(Class<S> serviceInterface);

  /**
   * Looks up the service with the given name and returns it.
   * 
   * @param name
   *          the name of the service to return.
   * @return an {@link Object} matching the given name.
   */
  public Object lookup(String name);

  /**
   * @return the {@link Properties} of this Corus instance.
   */
  public Properties getCorusProperties();

}