package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * Holds criteria to select shell scripts.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptCriteria implements Externalizable {

  static final long serialVersionUID = 1L;

  private ArgMatcher alias = ArgMatchers.any();

  /**
   * @param alias
   *          the {@link ArgMatcher} instance corresponding to a script alias.
   * @return this instance.
   */
  public ShellScriptCriteria setAlias(ArgMatcher alias) {
    this.alias = alias;
    return this;
  }

  /**
   * @return this instance's {@link ArgMatcher} instance corresponding to a script
   *         alias.
   */
  public ArgMatcher getAlias() {
    return alias;
  }

  /**
   * @return a new {@link ShellScriptCriteria}.
   */
  public static ShellScriptCriteria newInstance() {
    return new ShellScriptCriteria();
  }

  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.alias = (ArgMatcher) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(alias);
  }

}
