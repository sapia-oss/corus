package org.sapia.corus.core;

import java.io.File;

/**
 * This interface holds common property names.
 *
 * @author Yanick Duchesne
 */
public interface CorusConsts {

  public static final File CORUS_USER_HOME = new File(System.getProperty("user.home") + File.separator + ".corus");

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
   * @deprecate
   */
  public static final String PROPERTY_CORUS_MCAST_PORT               = "corus.server.multicast.port";

  public static final String CORUS_TRANSPORT_TCP                     = "tcp";

  // --------------------------------------------------------------------------
  // File & security-related


  /**
   * Corresponds to the property for setting the keypair generation algorithm.
   */
  public static final String PROPERTY_KEYPAIR_ALGO                   = "corus.keypair.algo";

  /**
   * Corresponds to the property for setting the cipher generation algorithm.
   */
  public static final String PROPERTY_CIPHER_ALGO                    = "corus.cipher.algo";

  /**
   * Defines the property prefix of a symbolic link definition.
   */
  public static final String PROPERTY_CORUS_FILE_LINK_PREFIX         = "corus.server.file.link.";

  /**
   * Defines the patterns that will determine which resources should be hidden.
   */
  public static final String PROPERTY_CORUS_FILE_HIDE_PATTERNS       = "corus.server.file.hide.patterns";

  /**
   * Defines the patterns that will determine which properties should be hidden.
   */
  public static final String PROPERTY_CORUS_PROPERTY_HIDE_PATTERNS   = "corus.server.property.hide.patterns";

  // --------------------------------------------------------------------------
  // REST

  /**
   * Indicates if authentication should be required for all REST calls - including <code>GET</code> ones.
   */
  public static final String PROPERTY_CORUS_REST_AUTH_REQUIRED       = "corus.server.api.auth.required";

  /**
   * Indicast the size for the client connector pool used in the REST extension.
   */
  public static final String PROPERTY_CORUS_REST_CONNECTOR_SIZE      = "corus.server.api.client.connector.pool-size";

  public static final String PROPERTY_CORUS_SERVER_LENIENT_ENABLED   = "corus.server.cluster.lenient-enabled";

  // --------------------------------------------------------------------------
  // Hot Config

  /**
   * Indicates if configuration changes are dynamically updated to running processes (hot config).
   */
  public static final String PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED = "corus.server.process.config-update.enabled";

  // --------------------------------------------------------------------------
  // Diagnostics

  /**
   * Indicates the amount of time given to Corus to boot up and start processes.
   */
  public static final String PROPERTY_CORUS_DIAGNOSTIC_GRACE_PERIOD_DURATION = "corus.server.diagnostic.grace-period.duration";

  // --------------------------------------------------------------------------
  // Consul

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

  // --------------------------------------------------------------------------
  // AWS

  /**
   * Indicates if AWS integration is enabled.
   */
  public static final String PROPERTY_CORUS_AWS_ENABLED                = "corus.server.aws.enabled";

  /**
   * Indicates if AWS CloudWatch integration is enabled.
   */
  public static final String PROPERTY_CORUS_AWS_CLOUDWATCH_ENABLED     = "corus.server.aws.cloudwatch.enabled";

  // --------------------------------------------------------------------------
  // Docker

  /**
   * Indicates if Docker integration is enabled.
   */
  public static final String PROPERTY_CORUS_DOCKER_ENABLED             = "corus.server.docker.enabled";

  public static final String PROPERTY_CORUS_DOCKER_CLIENT_EMAIL        = "corus.server.docker.client.email";

  public static final String PROPERTY_CORUS_DOCKER_CLIENT_USERNAME     = "corus.server.docker.client.username";

  public static final String PROPERTY_CORUS_DOCKER_CLIENT_PASSWORD     = "corus.server.docker.client.password";

  public static final String PROPERTY_CORUS_DOCKER_REGISTRY_ADDRESS    = "corus.server.docker.registry.address";

  public static final String PROPERTY_CORUS_DOCKER_REGISTRY_ENABLED    = "corus.server.docker.registry.sync-enabled";

  public static final String PROPERTY_CORUS_DOCKER_AUTO_REMOVE_ENABLED = "corus.server.docker.image-auto-remove-enabled";

  public static final String PROPERTY_CORUS_DOCKER_DAEMON_URL          = "corus.server.docker.daemon.url";

  public static final String PROPERTY_CORUS_DOCKER_CERTIFICATES_PATH   = "corus.server.docker.certificates.path";


  // --------------------------------------------------------------------------
  // NUMA

  /** Indicates if NUMA integration is enabled. */
  public static final String PROPERTY_CORUS_NUMA_ENABLED             = "corus.server.numa.enabled";

  /** Indicates if the auto-detection of NUMA is enabled or not. */
  public static final String PROPERTY_CORUS_NUMA_AUTO_DETECT_ENABLED = "corus.server.numa.auto-detection.enabled";

  /** Indicates if corus should bind process to numa cpu nodes or not. */
  public static final String PROPERTY_CORUS_NUMA_BIND_CPU            = "corus.server.numa.bind.cpu";

  /** Indicates if corus should bind process to numa memory slot or not. */
  public static final String PROPERTY_CORUS_NUMA_BIND_MEMORY         = "corus.server.numa.bind.memory";

  /** Indicates the number of numa nodes available on the current host. */
  public static final String PROPERTY_CORUS_NUMA_NODE_COUNT          = "corus.server.numa.node.count";

  /** Indicates the starting numa node id from which corus should start doing process binding. */
  public static final String PROPERTY_CORUS_NUMA_FIRST_NODE_ID       = "corus.server.numa.first.node.id";

}
