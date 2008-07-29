package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.interop.Status;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcStatus extends Status{
  
  private String _corusPid;
  private List   _contexts;
  
  ProcStatus(Process proc){
    _corusPid = proc.getProcessID();
    if(proc.getProcessStatus() == null){
      _contexts = new ArrayList(0);
    }
    else{
      _contexts = proc.getProcessStatus().getContexts();
    }
  }
  
  public List getContexts(){
    return _contexts;
  }
  
  public String getProcessID(){
    return _corusPid;
  }
  
}
