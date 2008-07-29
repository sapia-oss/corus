package org.sapia.corus.processor;

import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;


/**
 * Utility class that holds process-related information.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcessInfo {
  private Process       _process;
  private ProcessConfig _config;
  private Distribution  _dist;
  private boolean       _restart;

  /**
   * @param process the <code>Process</code> to which this instance corresponds.
   * @param dist the <code>Distribution</code> that this process comes from.
   * @param config the <code>ProcessConfig</code> that corresponds to the configuration (in corus.xml)
   * from which the given process was started.
   * @param restart if <code>true</code>, signals that the process to which this instance
   * corresponds is in restart mode.
   */
  public ProcessInfo(Process process, Distribution dist, ProcessConfig config,
              boolean restart) {
    _process = process;
    _config  = config;
    _restart = restart;
    _dist    = dist;
  }

  /**
   * @return a <code>Process</code>.
   */
  public Process getProcess() {
    return _process;
  }
  
  /**
   * @return a <code>Distribution</code>.
   */
  public Distribution getDistribution() {
    return _dist;
  }

  /**
   * @return a <code>ProcessConfig</code>
   */
  public ProcessConfig getConfig() {
    return _config;
  }
  
  /**
   * @return <code>true</code> if this process is in restart mode.
   */
  public boolean isRestart() {
    return _restart;
  }
}
