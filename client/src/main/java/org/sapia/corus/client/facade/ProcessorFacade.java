package org.sapia.corus.client.facade;

import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

/**
 * This interface specifies a facade to the Corus {@link Processor}
 * 
 * @author yduchesne
 *
 */
public interface ProcessorFacade {
  
  /**
   * Deploys the execution configuration whose file name is given.
   * 
   * @param fileName the name of the file of the execution configuration to deploy.
   * @param cluster
   */
  public void deployExecConfig(String fileName, ClusterInfo cluster) throws IOException, Exception;
  
  /**
   * Undeploys the execution configurations matching the given name. 
   * 
   * @param name the name of the exec config to undeploy.
   * @param cluster
   */
  public void undeployExecConfig(String name, ClusterInfo cluster);
  
  /**
   * Returns the {@link ExecConfig}s in the system.
   * 
   * @param cluster a {@link ClusterInfo} instance.
   * @return the {@link Results} containing the {@link ExecConfig}s in the system.
   */
  public  Results<List<ExecConfig>> getExecConfigs(ClusterInfo cluster);

  /**
   * Return the process whose identifier is given..
   *
   * @param pid a process identifier.
   * @return the {@link Process} instance whose ID is given.
   */
  public Process getProcess(String pid) throws ProcessNotFoundException;

  /**
   * Returns all process objects, per Corus server.
   *
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process} instances.
   */
  public Results<List<Process>> getProcesses(ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution whose corresponding processes should be returned.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process} instances.
   */
  public Results<List<Process>> getProcesses(String distName, ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution whose corresponding processes should be returned.
   * @param version the version of the distribution whose corresponding processes should be returned.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process} instances.
   */
  public Results<List<Process>> getProcesses(String distName, String version, ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution whose corresponding processes should be returned.
   * @param version the version of the distribution whose corresponding processes should be returned.
   * @param profile the profile whose corresponding processes should be returned.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process} instances.
   */
  public Results<List<Process>> getProcesses(String distName, 
        String version, 
        String profile, 
        ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution whose corresponding processes should be returned.
   * @param version the version of the distribution whose corresponding processes should be returned.
   * @param profile the profile whose corresponding processes should be returned.
   * @param processName the name whose corresponding processes should be returned.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process} instances.
   */
  public Results<List<Process>> getProcesses(String distName, String version, String profile,
                              String processName, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to an existing execution configuration.
   * 
   * @param configName the name of an execution configuration
   * @param cluster a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue exec(String configName, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to start
   * new process(es).
   * @param version the version of the distribution for which to start
   * new process(es).
   * @param profile the name of the profile under which the new process(es)
   * should be started.
   * @param the number of process(es) to start.
   * @param cluster a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue exec(String distName, String version, String profile,
                            int instances, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to start
   * new process(es).
   * @param version the version of the distribution for which to start
   * new process(es).
   * @param profile the name of the profile under which the new process(es)
   * should be started.
   * @param processName the name of the VM configuration for which new process(es)
   * should be started.
   * @param the number of process(es) to start.
   * @param cluster a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue exec(String distName, String version, String profile,
                            String processName, int instances, ClusterInfo cluster);

  /**
   * Restarts all suspended processes.
   * 
   * @param cluster a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue restart(ClusterInfo cluster);
  
  /**
   * Restarts the process with the given process UD.
   * @param pid a Corus process ID.
   */
  public void restart(String pid) throws ProcessNotFoundException;
  
  /**
   * Kills the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void kill(String distName, String version, String profile,
                   ClusterInfo cluster);

  /**
   * Kills the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the VM configuration for which to kill the running process(es).
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void kill(String distName, String version, String profile,
                   String processName, ClusterInfo cluster);

  /**
   * Kills the process with the given identifier.
   *
   * @param a process identifier.
   */
  public void kill(String pid) throws ProcessNotFoundException;

  /**
   * Suspends the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void suspend(String distName, String version, String profile,
                      ClusterInfo cluster);

  /**
   * Suspends the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the process configuration for which to kill the running process(es).
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void suspend(String distName, String version, String profile,
                      String processName, ClusterInfo cluster);

  /**
   * Suspends the process with the given identifier.
   *
   * @param pid a process identifier.
   */
  public void suspend(String pid) throws ProcessNotFoundException;
  
  /**
   * Return the status of the process whose identifier is given..
   *
   * @param pid a process identifier.
   * @return a {@link ProcStatus} instance.
   */
  public ProcStatus getStatusFor(String pid) throws ProcessNotFoundException;

  /**
   * Returns the status for all processes.
   * 
   * @param cluster a {@link ClusterInfo} instance.
   */
  public Results<List<ProcStatus>> getStatus(ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which process should have their status returned.
   * @param cluster a {@link ClusterInfo} instance.
   */
  public Results<List<ProcStatus>> getStatus(String distName, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param cluster a {@link ClusterInfo} instance.
   */
  public Results<List<ProcStatus>> getStatus(String distName, String version, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param cluster a {@link ClusterInfo} instance.
   */
  public Results<List<ProcStatus>> getStatus(String distName, String version, String profile, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param processName the name of the process for which to return process status.
   * @param cluster a {@link ClusterInfo} instance. 
   */
  public Results<List<ProcStatus>> getStatus(String distName, String version, String profile,
                           String processName, ClusterInfo cluster);  

}
