package org.sapia.corus.core;

import org.sapia.corus.client.Corus;

/**
 * Extends the {@link Corus} interface by specifying server-side only methods.
 * 
 * @author yduchesne
 *
 */
public interface InternalCorus extends Corus {

  /**
   * @param newDomainName the name of the domain to become a member of.
   */
  public void changeDomain(String newDomainName);
}
