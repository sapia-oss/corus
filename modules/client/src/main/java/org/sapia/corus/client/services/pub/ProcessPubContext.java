package org.sapia.corus.client.services.pub;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Strings;

/**
 * Holds data pertaining to the unpublishing/publishing of a process.
 * 
 * @author yduchesne
 *
 */
public class ProcessPubContext {
  
  private Process          process;
  private Distribution     distribution;
  private ProcessConfig    processConfig;
  private ActivePort       port;
  private ProcessPubConfig pubConfig;

  public ProcessPubContext(
      Process proc, 
      Distribution distribution, 
      ProcessConfig conf, 
      ActivePort port, 
      ProcessPubConfig config) {
    this.process       = proc;
    this.distribution  = distribution;
    this.processConfig = conf;
    this.port          = port;
    this.pubConfig     = config;
  }
  
  /**
   * @return the {@link Process} to publish.
   */
  public Process getProcess() {
    return process;
  }
  
  /**
   * @return the distribution of the process to publish.
   */
  public Distribution getDistribution() {
    return distribution;
  }
  
  /**
   * @return the {@link ProcessConfig} of the process to publish.
   */
  public ProcessConfig getProcessConfig() {
    return processConfig;
  }
  
  /**
   * @return the {@link ActivePort} to use for publishing.
   */
  public ActivePort getPort() {
    return port;
  }
  
  /**
   * @return this instance's {@link ProcessPubConfig}.
   */
  public ProcessPubConfig getPubConfig() {
    return pubConfig;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessPubContext) {
      ProcessPubContext other = (ProcessPubContext) obj;
      return process.equals(other.getProcess())
          && processConfig.equals(other.getProcessConfig())
          && pubConfig.equals(other.getPubConfig());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(process, processConfig, pubConfig);
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "process", process, "pubConfig", pubConfig);
  }
  
}