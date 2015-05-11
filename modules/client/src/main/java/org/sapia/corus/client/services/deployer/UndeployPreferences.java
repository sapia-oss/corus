package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.common.OptionalValue;

/**
 * Holds preferences used when undeploying.
 * 
 * @author yduchesne
 *
 */
public class UndeployPreferences implements Externalizable {
  
  static final long serialVersionUID = 1L;
  
  private OptionalValue<RevId> revId = OptionalValue.none();
  
  public UndeployPreferences revId(RevId revId) {
    this.revId = OptionalValue.of(revId);
    return this;
  }
  
  public OptionalValue<RevId> getRevId() {
    return revId;
  }
  
  public static UndeployPreferences newInstance() {
    return new UndeployPreferences();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.revId = (OptionalValue<RevId>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(revId);
  }

}
