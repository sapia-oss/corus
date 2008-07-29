package org.sapia.corus.security;

import java.rmi.Remote;

import org.sapia.corus.Module;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface SecurityModule extends Module, Remote{

  /** Defines the role name of this module. */  
  public static final String ROLE = SecurityModule.class.getName();

  /**
   * Set the pattern list of the allowed hosts that can connect to
   * this corus server.
   * 
   * @param patternList The pattern list of allowed hosts.
   */  
  public void setAllowedHostPatterns(String patternList);

  
  /**
   * Set the pattern list of the denied hosts that can't connect to
   * this corus server.
   * 
   * @param patternList The pattern list of denied hosts.
   */  
  public void setDeniedHostPatterns(String patternList);
}
