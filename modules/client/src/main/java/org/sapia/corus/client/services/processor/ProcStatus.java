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
  
  private String _corusPid;
  private List<Context>   _contexts;
  
  public ProcStatus(Process proc){
    _corusPid = proc.getProcessID();
    if(proc.getProcessStatus() == null){
      _contexts = new ArrayList<Context>(0);
    }
    else{
      _contexts = proc.getProcessStatus().getContexts();
    }
  }
  
  public List<Context> getContexts(){
    return _contexts;
  }
  
  public String getProcessID(){
    return _corusPid;
  }
  
}
