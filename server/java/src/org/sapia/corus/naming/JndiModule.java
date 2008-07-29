package org.sapia.corus.naming;

import org.sapia.corus.Module;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface JndiModule extends java.rmi.Remote, Module, RemoteContextProvider {
  public static final String ROLE = JndiModule.class.getName();

  public javax.naming.Context getContext();
}
