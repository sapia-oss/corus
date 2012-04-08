package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.processor.ExecConfig;

/**
 * Specifies methods for storing and retrieving {@link ExecConfig} instances.
 * 
 * @author yduchesne
 *
 */
public interface ExecConfigDatabase {

	/**
	 * @return this instance's {@link List} of {@link ExecConfig}s.
	 */
  public List<ExecConfig> getConfigs();

  /**
   * @return this instance's {@link List} of bootstrap {@link ExecConfig}s.
   */
  public List<ExecConfig> getBootstrapConfigs();

  /**
   * @param arg an {@link Arg} instance.
   * @return the {@link List} of {@link ExecConfig}s that match the given
   * {@link Arg} instance.
   */
  public List<ExecConfig> getConfigsFor(Arg arg);

  /**
   * Removes the {@link ExecConfig}s that correspond to the given {@link Arg}.
   * 
   * @param arg an {@link Arg} instance.
   */
  public void removeConfigsFor(Arg arg);

  /**
   * @param name the name of an expected {@link ExecConfig}.
   * @return the {@link ExecConfig} corresponding to the given name. 
   */
  public ExecConfig getConfigFor(String name);

  /**
   * Removes an {@link ExecConfig} from this instance.
   * 
   * @param name the name of the {@link ExecConfig} to remove.
   */
  public void removeConfig(String name);

  /**
   * Adds the given {@link ExecConfig} to this instance.
   * 
   * @param conf an {@link ExecConfig}.
   */
  public void addConfig(ExecConfig conf);

}