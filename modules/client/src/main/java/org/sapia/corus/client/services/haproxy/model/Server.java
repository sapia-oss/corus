package org.sapia.corus.client.services.haproxy.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;

public class Server implements Externalizable {
	
	private String name; 
	private String host;
	private int    port;
	
  /**
   * Do not calls: meant for externalization only.
   */
	public Server() {
  }
	
	public String getName() {
    return name;
  }

	public String getHost() {
    return host;
  }
	
	public int getPort() {
    return port;
  }
	
	// --------------------------------------------------------------------------
	// Externalizable
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	  name = in.readUTF();
	  host = in.readUTF();
	  port = in.readInt();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	  out.writeUTF(name);
	  out.writeUTF(host);
	  out.writeInt(port);
	}
	
	// --------------------------------------------------------------------------
	// Object overrides

	@Override
	public String toString() {
	  return "[name=" + name + ", host=" + host + ", port=" + port + "]";
	}
	
	@Override
	public int hashCode() {
	  return ObjectUtil.safeHashCode(host, port);
	}
	
	@Override
	public boolean equals(Object obj) {
	  if (obj instanceof Server) {
	    Server other = (Server) obj;
	    return name.equals(other.name) && port == other.port;
	  }
	  return false;
	}

}
