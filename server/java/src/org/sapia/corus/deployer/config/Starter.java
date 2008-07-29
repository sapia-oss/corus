package org.sapia.corus.deployer.config;

import org.sapia.console.CmdLine;

import org.sapia.corus.LogicException;


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
   * @param env an <code>Env</code> instance, holding the environment parameters
   * of the process whose command-line should be returned.
   * @return a <code>CmdLine</code>
   * @throws LogicException if the <code>CmdLine</code> could not be created.
   */
  public CmdLine toCmdLine(Env env) throws LogicException;
}
