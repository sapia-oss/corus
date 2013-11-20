package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;

/**
 * Holds criteria to select shell scripts.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptCriteria implements Externalizable {

  static final long serialVersionUID = 1L;

  private Arg alias = ArgFactory.any();

  /**
   * @param alias
   *          the {@link Arg} instance corresponding to a script alias.
   * @return this instance.
   */
  public ShellScriptCriteria setAlias(Arg alias) {
    this.alias = alias;
    return this;
  }

  /**
   * @return this instance's {@link Arg} instance corresponding to a script
   *         alias.
   */
  public Arg getAlias() {
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
    this.alias = (Arg) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(alias);
  }

}
