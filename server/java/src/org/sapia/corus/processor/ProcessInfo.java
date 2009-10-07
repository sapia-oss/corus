package org.sapia.corus.processor;

import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.Process;


/**
 * Utility class that holds process-related information.
 * 
 * @author Yanick Duchesne
 *
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
