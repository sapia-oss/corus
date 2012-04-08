package org.sapia.corus.client.services.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Status;

/**
 * @author Yanick Duchesne
 */
public class ProcStatus extends Status{
  
  static final long serialVersionUID = 1L;
  
  private String 			  corusPid;
  private List<Context> contexts;
  
  public ProcStatus(Process proc){
    corusPid = proc.getProcessID();
    if(proc.getProcessStatus() == null){
      contexts = new ArrayList<Context>(0);
    }
    else{
      contexts = proc.getProcessStatus().getContexts();
    }
  }
  
  public List<Context> getContexts(){
    return contexts;
  }
  
  public String getProcessID(){
    return corusPid;
  }
  
}
