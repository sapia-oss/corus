package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;

/**
 * A criteria implementation used to select {@link ExecConfig} instances.
 *  
 * @author yduchesne
 *
 */
public class ExecConfigCriteria implements Externalizable {
  
  static final long serialVersionUID = 1L;

  public static final class Builder {

    private Arg name;
    private int backup = 0;
    
    public Builder name(Arg name) {
      this.name = name;
      return this;
    }
    
    public Builder name(String name) {
      this.name = ArgFactory.exact(name);
      return this;
    }
    
    public Builder backup(int backup) {
      this.backup = backup;
      return this;
    }
    
    public Builder all() {
      this.name = ArgFactory.any();
      return this;
    }
    
    public ExecConfigCriteria build() {
      ExecConfigCriteria crit = new ExecConfigCriteria();
      crit.name = ArgFactory.anyIfNull(name);
      crit.backup = backup;
      return crit;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private Arg name;
  private int backup;
  
  public void setName(Arg name) {
    this.name = name;
  }
  
  public Arg getName() {
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
    name   = (Arg) in.readObject();
    backup = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeInt(backup);
  }
}
