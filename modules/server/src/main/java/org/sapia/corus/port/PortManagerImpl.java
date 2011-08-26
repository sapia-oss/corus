/*
 * PortManagerImpl.java
 *
 * Created on October 18, 2005, 7:05 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link PortManager} interface.
 * @author yduchesne
 */
@Bind(moduleInterface=PortManager.class)
@Remote(interfaces=PortManager.class)
public class PortManagerImpl extends ModuleHelper implements Service, PortManager {
  
  @Autowired
  private DbModule _db;
  private PortRangeStore _store;
  
  /** Creates a new instance of PortManagerImpl */
  public PortManagerImpl() {
  }
  
  protected PortManagerImpl(PortRangeStore store){
    _store = store;
  }
  
  public void init() throws Exception{
    _store = newPortRangeStore();
  }
  
  protected PortRangeStore newPortRangeStore() throws Exception{
    DbMap<String, PortRange> ports = _db.getDbMap(String.class, PortRange.class, "ports");
    return new PortRangeStore(ports);
  }
  
  public void start(){}
  
  public void dispose(){}
  
  public synchronized int aquirePort(String name) throws PortUnavailableException{
    if(!_store.containsRange(name)){
      throw new PortUnavailableException("Port range does not exist for: " + name);
    }
    PortRange range = (PortRange)_store.readRange(name);
    int port = range.acquire();
    _store.writeRange(range);
    logger().debug("Acquiring port: " + name + ":" + port);    
    return port;
  }
  
  public synchronized void releasePort(String name, int port){
    if(!_store.containsRange(name)){
      return;
    }
    PortRange range = (PortRange)_store.readRange(name);
    if(range.getMin() <= port && range.getMax() >= port){
      range.release(port);
      _store.writeRange(range);
    }
    logger().debug("Releasing port: " + name + ":" + port);
  }
  
  @Override
  public void addPortRanges(List<PortRange> ranges, boolean clearExisting)
      throws PortRangeInvalidException, PortRangeConflictException {
    if(clearExisting){
      _store.clear();
    }
    
    for(PortRange range : ranges){
      addPortRange(range);
    }
  }
  
  public synchronized void addPortRange(String name, int min, int max) 
    throws PortRangeInvalidException, PortRangeConflictException{
    addPortRange(new PortRange(name, min, max));
  }
  
  public synchronized void addPortRange(PortRange range)  
    throws PortRangeInvalidException, PortRangeConflictException{
    if(range.getMax() < range.getMin()){
      throw new PortRangeInvalidException("Max port must be greater than min port for: " + range);
    }
    if(_store.containsRange(range.getName())){
      throw new PortRangeConflictException("Port range already exists for: " + range.getName());
    }
    Iterator<PortRange> ranges = _store.getPortRanges();
    while(ranges.hasNext()){
      PortRange existing = (PortRange)ranges.next();
      if(existing.isConflicting(range)){
        throw new PortRangeConflictException("Existing port range (" + existing.getName() +
          ") conflicting with new range");
      }
    }
    _store.writeRange(range);
  }  
  
  public synchronized void removePortRange(Arg name, boolean force) throws PortActiveException{
    Collection<PortRange> ranges = _store.readRange(name);
    
    for(PortRange range : ranges){
      if(range.hasBusyPorts()){
        if(!force){
          throw new PortActiveException("Range has ports for which processes are running");
        }
      }      
    }
    for(PortRange range : ranges){
      _store.deleteRange(range.getName());      
    }    
  }  
  
  public synchronized void releasePortRange(final Arg name){
    Collection<PortRange> ranges = _store.readRange(name);
    for(PortRange range : ranges){
      range.releaseAll();
      _store.writeRange(range);      
    }
  }
  
  public synchronized List<PortRange> getPortRanges(){
    List<PortRange> lst = new ArrayList<PortRange>();
    Iterator<PortRange> ranges = _store.getPortRanges();
    while(ranges.hasNext()){
      PortRange range = (PortRange)ranges.next();
      if(logger().isDebugEnabled()){
        logger().debug("Returning port range: " + range.getName() + "[" + range.getMin() + " - " + range.getMax() + "]");
      }
      lst.add(range);    
    }
    Collections.sort(lst);
    return lst;
  }
  
  
  public String getRoleName(){
    return ROLE;
  }
}
