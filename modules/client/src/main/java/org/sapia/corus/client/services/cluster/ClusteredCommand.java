package org.sapia.corus.client.services.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.transport.CorusModuleOID;
import org.sapia.corus.client.transport.CorusOID;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.util.Assertions;


/**
 * @author Yanick Duchesne
 */
public class ClusteredCommand extends InvokeCommand {
  
  private           Set<ServerAddress>  targeted    = new HashSet<ServerAddress>();
  private           Set<ServerAddress>  visited     = new HashSet<ServerAddress>();
  private transient CorusCallback       callback;
  private           boolean             disable;
  
  /* Do not call; used for externalization only */
  public ClusteredCommand() {
  }

  public ClusteredCommand(InvokeCommand cmd) {
  	super(cmd.getOID(), cmd.getMethodName(), cmd.getParams(), cmd.getParameterTypes(), null);
  }
  
  public void addTargets(Set<ServerAddress> targets) {
    targeted.addAll(targets);
  }
  
  public void setCallback(CorusCallback callback) {
    this.callback = callback;
  }
  
  @Override
  protected Object doGetObjectFor(OID oid) throws NoSuchObjectException {
    if (oid instanceof CorusModuleOID) {
      return callback.getCorus().lookup(((CorusModuleOID) oid).getModuleName());
    } else if (oid instanceof CorusOID) {
      return callback.getCorus();
    }
    return super.doGetObjectFor(oid);
  }
  
	public Object execute() throws Throwable {
	  Assertions.illegalState(callback == null, "Corus callback not set");

	  // not clustering if command does not target a module.
	  disable = !(getOID() instanceof CorusModuleOID);

    try {
      if (callback.isDebug()) {
        callback.debug(String.format("Processing clustered command %s on %s", getMethodName(), getOID()));
      }
      Object returnValue = null;
      
      if (disable) {
        if (callback.isDebug()) {
          callback.debug(String.format("Command %s does not target a Corus module (will be executed on this host)", getMethodName()));
        }                
        returnValue = super.execute();
        
      // if the target set is empty, or if this node is specifically targeted (in the target set)
      } else if (targeted.isEmpty() || targeted.contains(callback.getCorus().getHostInfo().getEndpoint().getServerAddress())) {
        if (callback.isDebug()) {
          callback.debug(String.format("Command %s will be executed on this host", getMethodName()));
        }        
        returnValue = super.execute();
        visited.add(callback.getCorus().getHostInfo().getEndpoint().getServerAddress());
        ServerAddress nextAddress = selectNextAddress();
        if (nextAddress != null) {
          cascade(nextAddress);
        }
      // otherwise, this command is not meant for this node (cascading to the next host if required).
      } else {
        if (callback.isDebug()) {
          callback.debug(String.format("Not executing clustered command %s on %s; host not targeted: %s", 
              getMethodName(),  getOID(), callback.getCorus().getHostInfo().getEndpoint().getServerAddress()));
        }
        visited.add(callback.getCorus().getHostInfo().getEndpoint().getServerAddress());
        ServerAddress nextAddress = selectNextAddress();
        if (nextAddress != null) {
          returnValue = cascade(nextAddress);
        }
      }
      return returnValue;
    } catch (Throwable t) {
      callback.error("Error processing clustered command", t);
      throw t;
    }
	}
	
	private Object cascade(ServerAddress nextAddress) throws Throwable {
    if (callback.isDebug()) {
      callback.debug(String.format("Sending clustered command %s to %s", getMethodName(), nextAddress));
    }
    Object returnValue = callback.send(this, nextAddress);
    if (returnValue instanceof Throwable) {
      throw (Throwable) returnValue;
    }
    if (callback.isDebug()) {
      callback.debug(String.format("Receive %s from %s", returnValue, nextAddress));
    }	    
    return returnValue;
	}
	
	ServerAddress selectNextAddress() {
	  visited.add(callback.getCorus().getHostInfo().getEndpoint().getServerAddress());
	  Set<ServerAddress> siblings  = callback.getSiblings();
    if (callback.isDebug()) {
      callback.debug("Got siblings: " + siblings);
      callback.debug("Got visited: " + visited);
    }	  
	  return ClusteringHelper.selectNextTarget(visited, siblings);
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	  super.readExternal(in);
	  visited    = (Set<ServerAddress>) in.readObject();
	  targeted   = (Set<ServerAddress>) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	  super.writeExternal(out);
	  out.writeObject(visited);
	  out.writeObject(targeted);
	}
}
