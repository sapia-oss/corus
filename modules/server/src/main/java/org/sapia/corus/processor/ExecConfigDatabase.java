package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;

/**
 * Specifies methods for storing and retrieving {@link ExecConfig} instances.
 * 
 * @author yduchesne
 * 
 */
public interface ExecConfigDatabase extends Dumpable {

  /**
   * @return this instance's {@link List} of {@link ExecConfig}s.
   */
  public List<ExecConfig> getConfigs();

  /**
   * @return this instance's {@link List} of bootstrap {@link ExecConfig}s.
   */
  public List<ExecConfig> getBootstrapConfigs();

  /**
   * @param criteria
   *          The {@link ExecConfigCriteria} to use.
   * @return the {@link List} of {@link ExecConfig}s that match the given
   *          criteria.
   */
  public List<ExecConfig> getConfigsFor(ExecConfigCriteria criteria);

  /**
   * Removes the {@link ExecConfig}s that correspond to the given {@link ExecConfigCriteria}.
   * 
   * @param arg
   *          an {@link ExecConfigCriteria} instance.
   */
  public void removeConfigsFor(ExecConfigCriteria criteria);

  /**
   * @param name
   *          the name of an expected {@link ExecConfig}.
   * @return the {@link ExecConfig} corresponding to the given name.
   */
  public ExecConfig getConfigFor(String name);

  /**
   * Removes an {@link ExecConfig} from this instance.
   * 
   * @param name
   *          the name of the {@link ExecConfig} to remove.
   */
  public void removeConfig(String name);

  /**
   * Adds the given {@link ExecConfig} to this instance.
   * 
   * @param conf
   *          an {@link ExecConfig}.
   */
  public void addConfig(ExecConfig conf);
  
  /**
   * Removes the process references from the exec configs, if such process references have a corresponding
   * process definition in the given distribution.
   * 
   * @param dist a {@link Distribution}.
   */
  public void removeProcessesForDistribution(Distribution dist);
  
  /**
   * @param revId the revision ID to use when archiving.
   */
  public void archiveExecConfigs(RevId revId);
  
  /**
   * @param revId the revision ID for which to perform unarchiving.
   */
  public void unarchiveExecConfigs(RevId revId);

}