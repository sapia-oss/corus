package org.sapia.corus.http;

import org.sapia.corus.admin.Module;


/**
 * This module handles incoming HTTP requests from polling distributed VMs.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface HttpModule extends Module{

  
  public static String ROLE = HttpModule.class.getName();
  
  /***
   * Adds a <code>HTTPExtension</code> to this instance.
   * 
   * @param ext a <code>HTTPExtension</code>.
   */
  public void addHttpExtension(HttpExtension ext);
  
}
