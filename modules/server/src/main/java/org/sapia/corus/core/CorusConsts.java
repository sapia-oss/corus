package org.sapia.corus.core;

import java.io.File;

/**
 * This interface holds common property names.
 * 
 * @author Yanick Duchesne
 */
public interface CorusConsts {

  /**
   * Corresponds to the property key to which the Corus domain is associated.
   */
  public static final String PROPERTY_CORUS_DOMAIN                   = "corus.server.domain";

  /**
   * Corresponds to the property key to which the Corus server port is
   * associated.
   */
  public static final String PROPERTY_CORUS_PORT                     = "corus.server.port";

  /**
   * Corresponds to the property key to which the Corus server transport.
   */
  public static final String PROPERTY_CORUS_TRANSPORT                = "corus.server.transport";

  /**
   * Corresponds to the property indicating if repository functionality is
   * enabled or not.
   */
  public static final String PROPERTY_REPO_TYPE                      = "corus.server.repository.node.type";

  /**
   * Corresponds to the property that defines to which network interface(s) the
   * Corus server will be bound.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_ADDRESS_PATTERN          = "corus.server.address.pattern";

  /**
   * Corresponds to the property that defines the multicast address.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_MCAST_ADDRESS            = "corus.server.multicast.address";

  /**
   * Corresponds to the property that defines the multicast port.
   * 
   * @deprecated
   */
  public static final String PROPERTY_CORUS_MCAST_PORT               = "corus.server.multicast.port";

  public static final String CORUS_TRANSPORT_TCP                     = "tcp";

  /**
   * Defines the property prefix of a symbolic link definition.
   */
  public static final String PROPERTY_CORUS_FILE_LINK_PREFIX         = "corus.server.file.link.";
  
  /**
   * Defines the patterns that will determine which resources should be hidden.
   */
  public static final String PROPERTY_CORUS_FILE_HIDE_PATTERNS       = "corus.server.file.hide.patterns";
  
  /**
   * Indicates if authentication should be required for all REST calls - including <code>GET</code> ones.
   */
  public static final String PROPERTY_CORUS_REST_AUTH_REQUIRED       = "corus.server.api.auth.required";
  
  /**
   * Indicates if configuration changes are dynamically updated to running processes (hot config). 
   */
  public static final String PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED = "corus.server.process.config-update.enabled"; 

  /**
   * Indicates if publishing to Consul is enabled.
   */
  public static final String PROPERTY_CORUS_EXT_PUB_CONSUL_ENABLED   = "corus.server.pub.consul.enabled";
  
  /**
   * Defines the Consul agent URL.
   */
  public static final String PROPERTY_CORUS_EXT_PUB_CONSUL_AGENT_URL = "corus.server.pub.consul.agent-url";
  
  /**
   * Defines the interval (in seconds) at which Corus will publish its health to Consul.
   */
  public static final String PROPERTY_CORUS_EXT_PUB_CONSUL_INTERVAL  = "corus.server.pub.consul.interval";

  /**
   * Defines the TTL of Corus' publication to Consul (should be set to more than the publishing interval).
   */
  public static final String PROPERTY_CORUS_EXT_PUB_CONSUL_TTL       = "corus.server.pub.consul.ttl";
  
  public static final File CORUS_USER_HOME = new File(System.getProperty("user.home") + File.separator + ".corus");
  
  
}
