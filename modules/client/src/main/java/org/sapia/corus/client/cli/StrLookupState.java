package org.sapia.corus.client.cli;

import org.apache.commons.lang.text.StrLookup;

/**
 * Keeps a mutable reference on a {@link StrLookup} instance that is used as part of command-line processing, for
 * variable interpolation.
 * 
 * @author yduchesne
 *
 */
public class StrLookupState {
  
  private StrLookup vars;

  /**
   * @param vars the original {@link StrLookup} to wrap.
   */
  public StrLookupState(StrLookup vars) {
    this.vars = vars;
  }
  
  /**
   * @param vars a {@link StrLookup} to encapsulate.
   */
  public void set(StrLookup vars) {
    this.vars = vars;
  }
  
  /**
   * @return the encapsulated {@link StrLookup}.
   */
  public StrLookup get() {
    return vars;
  }

}
