package org.sapia.corus.processor;

public class StartupLock {
  
  private long _lastStartup;
  private long _interval;
  
  public StartupLock(long interval){
    _interval = interval;
  }
  
  public synchronized boolean authorize(){
    if(System.currentTimeMillis() - _lastStartup >= _interval){
      _lastStartup = System.currentTimeMillis();
      return true;
    }
    else{
      return false;
    }
  }
  
  void setInterval(long interval){
    _interval = interval;
  }
  
  long getInterval(){
    return _interval;
  }
  

}
