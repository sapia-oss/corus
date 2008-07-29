package org.sapia.corus.interop.api;


/**
 * Holds the constants that correspond to the names of the command-line properties that are
 * passed to dynamic processes. See the Corus Interop spec. for more details.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Consts {
  /**
   * Corresponds to the <code>corus.process.id</code> property.
   */
  public static final String CORUS_PID = "corus.process.id";

  /**
   * Corresponds to the <code>corus.server.host</code> property.
   */
  public static final String CORUS_SERVER_HOST = "corus.server.host";

  /**
   * Corresponds to the <code>corus.server.port</code> property.
   */
  public static final String CORUS_SERVER_PORT = "corus.server.port";

  /**
   * Corresponds to the <code>corus.process.poll.interval</code> property.
   */
  public static final String CORUS_POLL_INTERVAL = "corus.process.poll.interval";

  /**
   * Corresponds to the <code>corus.process.status.interval</code> property.
   */
  public static final String CORUS_STATUS_INTERVAL = "corus.process.status.interval";

  /**
   * Corresponds to the <code>corus.client.analysis.interval</code> property.
   */
  public static final String CORUS_CLIENT_ANALYSIS_INTERVAL = "corus.client.analysis.interval";

  /**
   * Corresponds to the <code>corus.distribution.dir</code> property.
   */
  public static final String CORUS_DIST_DIR = "corus.distribution.dir";

  /**
   * Corresponds to the <code>corus.distribution.version</code> property.
   */
  public static final String CORUS_DIST_VERSION = "corus.distribution.version";

  /**
   * Corresponds to the <code>corus.distribution.name</code> property.
   */
  public static final String CORUS_DIST_NAME = "corus.distribution.name";

  /**
   * Corresponds to the <code>corus.process.dir</code> property.
   */
  public static final String CORUS_PROCESS_DIR = "corus.process.dir";
  
  /**
   * Corresponds to the <code>corus.process.log.level</code> property.
   */  
  public static final String CORUS_PROCESS_LOG_LEVEL = "corus.process.log.level";
  
  public static final String LOG_LEVEL_DEBUG  = "debug";
  
  public static final String LOG_LEVEL_INFO  = "info";
  
  public static final String LOG_LEVEL_WARN  = "warn";
  
  public static final String LOG_LEVEL_FATAL  = "fatal";
}
