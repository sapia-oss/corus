package org.sapia.corus.client.services.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.ubik.module.ModuleNotFoundException;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.oid.OID;


/**
 * @author Yanick Duchesne
 */
public class ClusteredCommand extends InvokeCommand {
  
  private           Set<ServerAddress>  visited     = new HashSet<ServerAddress>();
  private transient CorusCallback       callback;
  private           String              moduleName;
  private           boolean             disable;
  
  /* Do not call; used for externalization only */
  public ClusteredCommand() {
  }

  public ClusteredCommand(InvokeCommand cmd, ClusteredInvoker invoker, Set<ServerAddress> targets) {
  	super(cmd.getOID(), cmd.getMethodName(), cmd.getParams(), cmd.getParameterTypes(), null);
  }
  
  public void setCallback(CorusCallback callback) {
    this.callback = callback;
  }
  
  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }
  
  @Override
  protected Object doGetObjectFor(OID oid) throws NoSuchObjectException {
    if (moduleName != null) {
      try {
        return callback.lookup(moduleName);
      } catch (ModuleNotFoundException e) {
        throw new NoSuchObjectException("No object found for module: " + moduleName);
      }
    } else {
      Object target = super.doGetObjectFor(oid);
      if (target instanceof Module) {
        this.moduleName = ((Module) target).getRoleName();
      } else {
        disable = true;
      }
      return target;
    }
  }
  
	public Object execute() throws Throwable {
	  if (callback == null) {
	    throw new IllegalStateException("Corus callback not set");
	  }
	  
    try {
      callback.debug("Executing clustered command " + getMethodName());
      Object returnValue = super.execute();
      if (!disable) {
        visited.add(callback.getCorusAddress());
        cascade();
      }
      return returnValue;
    } catch (Throwable t) {
      callback.error("Error processing clustered command", t);
      throw t;
    }
	}
	
	private void cascade() throws Throwable {
	  ServerAddress nextAddress = selectNextAddress();
	  if (nextAddress != null) {
	    callback.debug("Sending clustered command " + getMethodName() + " to " + nextAddress);
	    Object returnValue = callback.send(this, nextAddress);
	    if (returnValue instanceof Throwable) {
	      throw (Throwable) returnValue;
	    }	    
	  } else {
	    callback.debug("No more host to target");
	  }
	}
	
	ServerAddress selectNextAddress() {
	  Set<ServerAddress> siblings = callback.getSiblings();
	  Set<ServerAddress> remaining = new HashSet<ServerAddress>(siblings);
	  remaining.removeAll(visited);
    if (callback.isDebug()) {
      callback.debug("Got siblings: " + siblings);
      callback.debug("Remaining to visit: " + remaining);      
    }	  
	  if (remaining.isEmpty()) {
	    return null;
	  }
	  return remaining.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	  super.readExternal(in);
	  visited = (Set<ServerAddress>) in.readObject();
	  moduleName = (String) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	  super.writeExternal(out);
	  out.writeObject(visited);
	  out.writeObject(moduleName);
	}
}
