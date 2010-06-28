package org.sapia.corus.client.services.deployer.dist;

import java.util.List;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.exceptions.misc.MissingDataException;


/**
 * Specifies the behavior of classes that creates command lines.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Starter {
  
  /**
   * @param profile the profile under which the process corresponding to this
   * instance should be started.
   */
  public void setProfile(String profile);

  /**
   * @return the profile under which the process corresponding to this
   * instance should be started.
   */  
  public String getProfile();
  
  /**
   * @return this instances dependencies.
   */
  public List<Dependency> getDependencies();
  
  /**
   * @param env an {@link Env} instance, holding the environment parameters
   * of the process whose command-line should be returned.
   * @return a {@link CmdLine}
   * @throws MissingDataException if the command-line object could not be created.
   */
  public CmdLine toCmdLine(Env env) throws MissingDataException;
}
