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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;

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
  
  public Collection<PortRange> readRange(final Arg name){
    return new IteratorFilter<PortRange>(new Matcher<PortRange>() {
      @Override
      public boolean matches(PortRange range) {
        return name.matches(range.getName());
      }
    }).filter(_ranges.values()).sort(new Comparator<PortRange>() {
      
      @Override
      public int compare(PortRange o1, PortRange o2) {
        return o1.getName().compareTo(o2.getName());
      }
    }).get();
  }  
  
  public void deleteRange(String name){
    _ranges.remove(name);
  }
  
  public void clear(){
    _ranges.clear();
  }
}
