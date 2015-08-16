package org.sapia.corus.client.services.deployer.dist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Base behavior for diagnostics-related config classes.
 * 
 * @author yduchesne
 *
 */
public abstract class DiagnosticConfig implements Externalizable {
  
  static final long serialVersionUID = 1L;
  
  public static final int DEFAULT_GRACE_PERIOD_SECONDS = 45;
  
  private String protocol;
  
  private int gracePeriod = DEFAULT_GRACE_PERIOD_SECONDS;
  
  public DiagnosticConfig(String protocol) {
    this.protocol = protocol;
  }
  
  public String getProtocol() {
    return protocol;
  }
  
  public void setGracePeriod(int gracePeriod) {
    this.gracePeriod = gracePeriod;
  }
  
  public int getGracePeriod() {
    return gracePeriod;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    protocol = in.readUTF();
    gracePeriod = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(protocol);
    out.writeInt(gracePeriod);
  }

}
