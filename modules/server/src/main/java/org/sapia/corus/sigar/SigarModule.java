package org.sapia.corus.sigar;

import org.hyperic.sigar.Sigar;


/**
 * Initializes a {@link Sigar} if the <code>java.library.path</code> JVM property has been defined.
 * <p>
 * This interface is not specified as part of the client module since the implementation is meant
 * to be accessible server-side only (no remote access).
 *
 * @author yduchesne
 *
 */
public interface SigarModule {

  public static final String ROLE = SigarModule.class.getName();

}
