package org.sapia.corus.db;

import org.sapia.corus.admin.Module;


/**
 * This module provides a persistency service to other modules.
 *
 * @author Yanick Duchesne
  * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DbModule extends Module {
  public static final String ROLE = DbModule.class.getName();

  /**
   * Returns a <code>DbMap</code> instance for the given name.
   * It is the caller's responsability to ensure that the passed
   * in name does not duplicate another; callers should used qualified
   * names to retrieve <code>DbMap</code> instances.
   *
   * @param name the logical name of the desired <code>DbMap</code>.
   * @return a new <code>DbMap</code> if none exists for the given name,
   * or an already existing one if this applies.
   */
  public DbMap getDbMap(String name);
}
