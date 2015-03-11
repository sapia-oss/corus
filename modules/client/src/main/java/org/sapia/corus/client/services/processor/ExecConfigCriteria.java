package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * A criteria implementation used to select {@link ExecConfig} instances.
 *  
 * @author yduchesne
 *
 */
public class ExecConfigCriteria implements Externalizable {
  
  static final long serialVersionUID = 1L;

  public static final class Builder {

    private ArgMatcher name;
    private int backup = 0;
    
    public Builder name(ArgMatcher name) {
      this.name = name;
      return this;
    }
    
    public Builder name(String name) {
      this.name = ArgMatchers.exact(name);
      return this;
    }
    
    public Builder backup(int backup) {
      this.backup = backup;
      return this;
    }
    
    public Builder all() {
      this.name = ArgMatchers.any();
      return this;
    }
    
    public ExecConfigCriteria build() {
      ExecConfigCriteria crit = new ExecConfigCriteria();
      crit.name = ArgMatchers.anyIfNull(name);
      crit.backup = backup;
      return crit;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private ArgMatcher name;
  private int backup;
  
  public void setName(ArgMatcher name) {
    this.name = name;
  }
  
  public ArgMatcher getName() {
    return name;
  }
  
  public void setBackup(int backup) {
    this.backup = backup;
  }
  
  public int getBackup() {
    return backup;
  }
  
  public static final Builder builder() {
    return new Builder();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  public void readExternal(java.io.ObjectInput in) throws java.io.IOException ,ClassNotFoundException {
    name   = (ArgMatcher) in.readObject();
    backup = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeInt(backup);
  }
}
