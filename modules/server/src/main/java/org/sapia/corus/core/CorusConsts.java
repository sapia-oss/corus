package org.sapia.corus.core;

/**
 * This interface holds common property names.
 * 
 * @author Yanick Duchesne
 */
public interface CorusConsts {

  /**
   * Corresponds to the property key to which the Corus domain is associated.
   */
  public static final String PROPERTY_CORUS_DOMAIN = "corus.server.domain";

  /**
   * Corresponds to the property key to which the Corus server port is
   * associated.
   */
  public static final String PROPERTY_CORUS_PORT = "corus.server.port";

  /**
   * Corresponds to the property key to which the Corus server transport.
   */
  public static final String PROPERTY_CORUS_TRANSPORT = "corus.server.transport";

  /**
   * Corresponds to the property indicating if repository functionality is
   * enabled or not.
   */
  public static final String PROPERTY_REPO_TYPE = "corus.server.repository.node.type";

  /**
   * Corresponds to the property that defines to which network interface(s) the
   * Corus server will be bound.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_ADDRESS_PATTERN = "corus.server.address.pattern";

  /**
   * Corresponds to the property that defines the multicast address.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_MCAST_ADDRESS = "corus.server.multicast.address";

  /**
   * Corresponds to the property that defines the multicast port.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_MCAST_PORT = "corus.server.multicast.port";

  public static final String CORUS_TRANSPORT_TCP = "tcp";

  /**
   * Defines the property prefix of a symbolic link definition.
   */
  public static final String PROPERTY_CORUS_FILE_LINK_PREFIX = "corus.server.file.link.";
  
}
