/*
 * PortRange.java
 *
 * Created on October 18, 2005, 6:46 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.client.services.port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;


/**
 * @author yduchesne
 */
public class PortRange extends AbstractPersistent<String, PortRange> implements java.io.Serializable, Comparable<PortRange>{
  
  static final long serialVersionUID = 1L;
  
  private String 				name;
  private int 				  min, max;
  private List<Integer> available = new ArrayList<Integer>();
  private List<Integer> active 		= new ArrayList<Integer>();
  
  PortRange(){}
  
  @Override
  @Transient
  public String getKey() {
    return name;
  }
  
  /** Creates a new instance of PortRange */
  public PortRange(String name, int min, int max) throws PortRangeInvalidException{
    if(min <= 0)
      throw new PortRangeInvalidException("Min port must be greater than 0");
    if(max <= 0)
      throw new PortRangeInvalidException("Max port must be greater than 0");    
    if(min > max)
      throw new PortRangeInvalidException("Min port must be lower than max port");    
    
    this.name = name;
    this.min = min;
    this.max = max;
    
    for(int i = min; i <= max; i++){
      available.add(new Integer(i));
    }
  }
  
  public String getName(){
    return name;
  }
  
  public List<Integer> getAvailable(){
    return available;
  }
  
  public List<Integer> getActive(){
    return active;
  }  
  
  /**
   * @return the lowerbound port in this range.
   */
  public int getMin(){
    return min;
  }
  
  /**
   * @return the higherbound port in this range.
   */
  public int getMax(){
    return max;
  }
  
  /**
   * @param port a port to release.
   */
  public synchronized void release(int port){
    Integer portObj = new Integer(port);
    active.remove(portObj);
    if(!available.contains(portObj)){
      available.add(portObj);
    }
    Collections.sort(available);
  }
  
  /**
   * acquires an available port from this instance.
   */
  public synchronized int acquire() throws PortUnavailableException{
    if(available.size() == 0){
      throw new PortUnavailableException("No port available for range: " + name);
    }
    Integer portObj = (Integer)available.remove(0);
    active.add(portObj);
    Collections.sort(active);    
    return portObj.intValue();
  }
  
  /**
   * @return <code>true</code> if this range has busy ports.
   */
  public boolean hasBusyPorts(){
    return active.size() > 0;
  }
  
  /**
   * releases all active ports.
   */
  public void releaseAll(){
    for(int i = 0; i < active.size(); i++){
      Integer busy = (Integer)active.get(i);
      if(!available.contains(busy)){
        available.add(busy);
      }
    }
    Collections.sort(available);    
    active.clear();
  }
  
  /**   
   * @return <code>true</code> if the given port range
   * is conflicting with this instance.
   */
  public boolean isConflicting(PortRange other){
    return (other.max <= max &&  other.max >= min) ||
      (other.min >= min &&  other.min <= max);
  }
  
  @Override
  public int compareTo(PortRange other) {
    return getName().compareTo(other.getName());
  }
  
  public String toString(){
    return new StringBuffer().append("[")
     .append("name=").append(name)
     .append(", min=").append(min)
     .append(", max=").append(max)
     .append("]").toString();
  }
  
}
