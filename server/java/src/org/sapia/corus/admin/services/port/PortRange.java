/*
 * PortRange.java
 *
 * Created on October 18, 2005, 6:46 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.admin.services.port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.exceptions.PortRangeInvalidException;


/**
 *
 * @author yduchesne
 */
public class PortRange implements java.io.Serializable{
  
  static final long serialVersionUID = 1L;
  
  private String _name;
  private int _min, _max;
  private List<Integer> _availablePorts = new ArrayList<Integer>();
  private List<Integer> _busyPorts = new ArrayList<Integer>();
  
  /** Creates a new instance of PortRange */
  public PortRange(String name, int min, int max) throws PortRangeInvalidException{
    if(min <= 0)
      throw new PortRangeInvalidException("Min port must be greater than 0");
    if(max <= 0)
      throw new PortRangeInvalidException("Max port must be greater than 0");    
    if(min >= max)
      throw new PortRangeInvalidException("Min port must be lower than max port");    
    
    _name = name;
    _min = min;
    _max = max;
    
    for(int i = min; i <= max; i++){
      _availablePorts.add(new Integer(i));
    }
  }
  
  public String getName(){
    return _name;
  }
  
  public List<Integer> getAvailable(){
    return Collections.unmodifiableList(_availablePorts);
  }
  
  public List<Integer> getActive(){
    return Collections.unmodifiableList(_busyPorts);
  }  
  
  /**
   * @return the lowerbound port in this range.
   */
  public int getMin(){
    return _min;
  }
  
  /**
   * @return the higherbound port in this range.
   */
  public int getMax(){
    return _max;
  }
  
  /**
   * @param port a port to release.
   */
  public synchronized void release(int port){
    Integer portObj = new Integer(port);
    _busyPorts.remove(portObj);
    if(!_availablePorts.contains(portObj)){
      _availablePorts.add(portObj);
    }
  }
  
  /**
   * acquires an available port from this instance.
   */
  public synchronized int acquire(){
    if(_availablePorts.size() == 0){
      throw new IllegalStateException("No port available for range: " + _name);
    }
    Integer portObj = (Integer)_availablePorts.remove(0);
    _busyPorts.add(portObj);
    return portObj.intValue();
  }
  
  /**
   * @return <code>true</code> if this range has busy ports.
   */
  public boolean hasBusyPorts(){
    return _busyPorts.size() > 0;
  }
  
  /**
   * releases all active ports.
   */
  public void releaseAll(){
    for(int i = 0; i < _busyPorts.size(); i++){
      Integer busy = (Integer)_busyPorts.get(i);
      if(!_availablePorts.contains(busy)){
        _availablePorts.add(busy);
      }
    }
    _busyPorts.clear();
  }
  
  /**   
   * @return <code>true</code> if the given port range
   * is conflicting with this instance.
   */
  public boolean isConflicting(PortRange other){
    return (other._max <= _max &&  other._max >= _min) ||
      (other._min >= _min &&  other._min <= _max);
  }
  
  public String toString(){
    return new StringBuffer().append("[")
     .append("name=").append(_name)
     .append(", min=").append(_min)
     .append(", max=").append(_max)
     .append("]").toString();
  }
  
}
