package org.sapia.corus.processor;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;

/**
 * Utility class that holds process-related information.
 * 
 * @author Yanick Duchesne
 * 
 */
public class ProcessInfo {

  private Process process;
  private ProcessConfig config;
  private Distribution dist;
  private boolean restart;

  /**
   * @param process
   *          the {@link Process} to which this instance corresponds.
   * @param dist
   *          the {@link Distribution} that this process comes from.
   * @param config
   *          the {@link ProcessConfig} that corresponds to the configuration
   *          (in corus.xml) from which the given process was started.
   * @param restart
   *          if <code>true</code>, signals that the process to which this
   *          instance corresponds is in restart mode.
   */
  public ProcessInfo(Process process, Distribution dist, ProcessConfig config, boolean restart) {
    this.process = process;
    this.config = config;
    this.restart = restart;
    this.dist = dist;
  }

  /**
   * @return a {@link Process}.
   */
  public Process getProcess() {
    return process;
  }

  /**
   * @return a {@link Distribution}.
   */
  public Distribution getDistribution() {
    return dist;
  }

  /**
   * @return a {@link ProcessConfig}
   */
  public ProcessConfig getConfig() {
    return config;
  }

  /**
   * @return <code>true</code> if this process is in restart mode.
   */
  public boolean isRestart() {
    return restart;
  }
}
