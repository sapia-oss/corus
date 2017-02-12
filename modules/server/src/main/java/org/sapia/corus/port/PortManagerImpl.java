package org.sapia.corus.port;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link PortManager} interface.
 * 
 * @author yduchesne
 */
@Bind(moduleInterface = PortManager.class)
@Remote(interfaces = PortManager.class)
public class PortManagerImpl extends ModuleHelper implements Service, PortManager {

  @Autowired
  private DbModule db;

  @Autowired
  private HttpModule httpModule;

  @Autowired
  private Processor processor;

  private PortRangeStore store;

  public PortManagerImpl() {
  }

  protected PortManagerImpl(PortRangeStore store, Processor processor) {
    this.store = store;
    this.processor = processor;
  }

  public void init() throws Exception {
    store = newPortRangeStore();
  }

  protected PortRangeStore newPortRangeStore() throws Exception {
    DbMap<String, PortRangeDefinition> ports = db.getDbMap(String.class, PortRangeDefinition.class, "ports");
    return new PortRangeStore(ports);
  }

  public void start() {
    try {
      PortManagerHttpExtension extension = new PortManagerHttpExtension(this);
      httpModule.addHttpExtension(extension);
    } catch (Exception e) {
      log.error("Could not add port management HTTP extension", e);
    }
  }

  public void dispose() {
  }
  
  @Override
  public String getRoleName() {
    return ROLE;
  }

  protected List<ActivePort> getActivePortsForName(String name) {
    return processor.getProcessesWithPorts().stream()
        .flatMap(p -> p.getActivePorts().stream())
        .filter(p -> StringUtils.equals(name, p.getName()))
        .sorted()
        .collect(Collectors.toList());
  }
  
  @Override
  public synchronized int aquirePort(String name) throws PortUnavailableException {
    PortRangeDefinition range = store.readRange(name);
    if (range == null) {
      throw new PortUnavailableException("Port range does not exist for: " + name);
    }

    List<Integer> allocatedPorts = getActivePortsForName(range.getName()).stream().
        map(ActivePort::getPort).
        collect(Collectors.toList());

    for (int i = range.getMin(); i <= range.getMax(); i++) {
      if (!allocatedPorts.contains(i)) {
        logger().debug("Acquiring port: " + name + ":" + i);
        return i;
      }
    }

    throw new PortUnavailableException("No port available for range: " + name);
  }

  @Override
  public synchronized void addPortRanges(List<PortRange> ranges, boolean clearExisting) throws PortRangeInvalidException, PortRangeConflictException {
    if (clearExisting) {
      store.clear();
    }

    for (PortRange pr: ranges) {
      doValidateAndAddPortRangeDefinition(
          new PortRangeDefinition(pr.getName(), pr.getMin(), pr.getMax()));
    }
  }
  
  @Override
  public synchronized void addPortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException {
    doValidateAndAddPortRangeDefinition(
        new PortRangeDefinition(name, min, max));
  }

  public synchronized void addPortRange(PortRange range) throws PortRangeInvalidException, PortRangeConflictException {
    doValidateAndAddPortRangeDefinition(
        new PortRangeDefinition(range.getName(), range.getMin(), range.getMax()));
  }

  protected void doValidateAndAddPortRangeDefinition(PortRangeDefinition def) throws PortRangeInvalidException, PortRangeConflictException {
    if (def.getMax() < def.getMin()) {
      throw new PortRangeInvalidException("Max port must be greater than min port for: " + def.getName());
    }
    if (store.containsRange(def.getName())) {
      throw new PortRangeConflictException("Port range already exists for: " + def.getName());
    }
    
    for (PortRangeDefinition existing: store.getPortRanges()) {
      if (areRangeConflicting(existing, def)) {
        throw new PortRangeConflictException("Existing port range (" + existing.getName() + ") conflicting with new range");
      }
    }

    store.writeRange(def);
  }
  
  protected boolean areRangeConflicting(PortRangeDefinition def, PortRangeDefinition anotherDef) {
    return (anotherDef.getMax() <= def.getMax() && anotherDef.getMax() >= def.getMin()) ||
           (anotherDef.getMin() >= def.getMin() && anotherDef.getMin() <= def.getMax()) ||
           (anotherDef.getMin() <= def.getMin() && anotherDef.getMax() >= def.getMax());
  }

  @Override
  public synchronized void updatePortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException {
    PortRangeDefinition def = new PortRangeDefinition(name, min, max);
    if (max < min) {
      throw new PortRangeInvalidException("Max port must be greater than min port for: " + def);
    }

    for (PortRangeDefinition existing: store.getPortRanges()) {
      if (!existing.getName().equals(def.getName()) && areRangeConflicting(existing, def)) {
        throw new PortRangeConflictException("Existing port range (" + existing.getName() + ") conflicting with range " + def);
      }
    }

    store.writeRange(def);
  }

  @Override
  public synchronized void removePortRange(ArgMatcher name, boolean force) throws PortActiveException {
    List<PortRangeDefinition> ranges = store.readRange(name);

    for (PortRangeDefinition existing: ranges) {
      if (getActivePortsForName(existing.getName()).size() > 0 && !force) {
        throw new PortActiveException("Range " + existing.getName() + " has ports for which processes are running");
      }
    }
    
    for (PortRangeDefinition existing: ranges) {
      store.deleteRange(existing.getName());
    }
  }

  @Override
  public synchronized List<PortRange> getPortRanges() {
    try {
      List<PortRange> created = new ArrayList<PortRange>();
      for (PortRangeDefinition def: store.getPortRanges()) {
        PortRange range = new PortRange(def.getName(), def.getMin(), def.getMax());
        getActivePortsForName(def.getName()).stream().forEach(p -> range.acquire(p.getPort()));
        created.add(range);
      }
      
      Collections.sort(created);
      return created;
      
    } catch (PortRangeInvalidException prie) {
      throw new IllegalStateException("Caugh an invalid port range from the db store", prie);
    }
  }

  @Override
  public synchronized List<PortRange> getPortRanges(ArgMatcher matcher) {
    try {
      List<PortRange> retrieved = new ArrayList<PortRange>();
      for (PortRangeDefinition def: store.readRange(matcher)) {
        PortRange range = new PortRange(def.getName(), def.getMin(), def.getMax());
        getActivePortsForName(def.getName()).stream().forEach(p -> range.acquire(p.getPort()));
        retrieved.add(range);
      }
      
      Collections.sort(retrieved);
      return retrieved;
      
    } catch (PortRangeInvalidException prie) {
      throw new IllegalStateException("Caugh an invalid port range from the db store", prie);
    }
  }
  
  @Override
  public synchronized void archive(RevId revId) {
    store.archiveRanges(revId);
  }
  
  @Override
  public synchronized void unarchive(RevId revId) {
    store.unarchiveRanges(revId);
  }
  
  @Override
  public synchronized void dump(JsonStream stream) {
    stream.field("portRanges").beginObject();
    store.dump(stream);
    stream.endObject();
  }
  
  @Override
  public synchronized void load(JsonInput dump) {
    store.load(dump.getObject("portRanges"));
  }

}
