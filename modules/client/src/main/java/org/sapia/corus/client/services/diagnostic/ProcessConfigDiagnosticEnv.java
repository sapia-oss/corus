package org.sapia.corus.client.services.diagnostic;

import java.util.List;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.TimeValue;

/**
 * This interface isolates the {@link ProcessConfigDiagnosticResult} from the server-side implementation.
 * 
 * @author yduchesne
 *
 */
public interface ProcessConfigDiagnosticEnv {

  /**
   * @return the {@link Distribution} in the context of which the given processes are being evaluated.
   */
  public Distribution getDistribution();
  
  /**
   * @return the {@link ProcessConfig} in the context of which the given processes are being evaluated.
   */
  public ProcessConfig getProcessConfig();
  
  /**
   * @return the {@link List} of processes to diagnose.
   */
  public List<Process> getProcesses();
  
  /**
   * @return the number of processes that are expected.
   */
  public int getExpectedInstanceCount();
  
  /**
   * @return <code>true</code> if the grace period is not yet exhausted.
   */
  public boolean isWithinGracePeriod();
  
  /**
   * @return <code>true</code> if the grace period has exhausted.
   */
  public boolean isGracePeriodExhausted();
  
  /**
   * @return the {@link TimeValue} corresponding to the grace period.
   */
  public TimeValue getGracePeriod();
}
