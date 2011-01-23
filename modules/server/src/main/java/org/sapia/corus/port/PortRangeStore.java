/*
 * NewClass.java
 *
 * Created on October 18, 2005, 7:06 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.port;

import java.util.Iterator;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.port.PortRange;

/**
 *
 * @author yduchesne
 */
public class PortRangeStore {
  
  private DbMap<String, PortRange> _ranges;
  
  public PortRangeStore(DbMap<String, PortRange> ranges) {
    _ranges = ranges;
  }
  
  public Iterator<PortRange> getPortRanges(){
    return _ranges.values();
  }
  
  public void writeRange(PortRange range){
    _ranges.put(range.getName(), range);
  }
  
  public boolean containsRange(String name){
    return _ranges.get(name) != null;
  }
  
  public PortRange readRange(String name){
    return _ranges.get(name);
  }
  
  public void deleteRange(String name){
    _ranges.remove(name);
  }
}
