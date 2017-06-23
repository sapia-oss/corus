package org.sapia.corus.client.services.haproxy.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.common.ObjectUtil;

public class Backend implements Externalizable {
  
  private String                name;
  private Map<String, Server>   servers     = new HashMap<>();
  
  /**
   * Do not call: meant for externalization only.
   */
  public Backend() {
  }
  
  public Backend(String name) {
    this.name        = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void addServer(Server svr) {
    servers.put(svr.getName(), svr);
  }
  
  public Collection<Server> getServers() {
    return Collections.unmodifiableCollection(servers.values());
  }

  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    name = in.readUTF();
    servers = (Map<String, Server>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeObject(servers);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return "[name=" + name + "]";
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(name);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Backend) {
      Backend other = (Backend) obj;
      return name.equals(other.name);
    }
    return false;
  }
  
}
