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
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.TaskManager;
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
  private TaskManager taskMan;

  @Autowired
  private HttpModule httpModule;

  private PortRangeStore store;

  /** Creates a new instance of PortManagerImpl */
  public PortManagerImpl() {
  }

  protected PortManagerImpl(PortRangeStore store) {
    this.store = store;
  }

  public void init() throws Exception {
    store = newPortRangeStore();
  }

  protected PortRangeStore newPortRangeStore() throws Exception {
    DbMap<String, PortRange> ports = db.getDbMap(String.class, PortRange.class, "ports");
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

  public synchronized int aquirePort(String name) throws PortUnavailableException {
    if (!store.containsRange(name)) {
      throw new PortUnavailableException("Port range does not exist for: " + name);
    }
    PortRange range = (PortRange) store.readRange(name);
    int port = range.acquire();
    store.writeRange(range);
    logger().debug("Acquiring port: " + name + ":" + port);
    return port;
  }

  public synchronized void releasePort(String name, int port) {
    if (!store.containsRange(name)) {
      return;
    }
    PortRange range = (PortRange) store.readRange(name);
    if (range.getMin() <= port && range.getMax() >= port) {
      range.release(port);
      store.writeRange(range);
    }
    logger().debug("Releasing port: " + name + ":" + port);
  }

  @Override
  public synchronized void addPortRanges(List<PortRange> ranges, boolean clearExisting) throws PortRangeInvalidException, PortRangeConflictException {
    if (clearExisting) {
      store.clear();
    }

    for (PortRange range : ranges) {
      addPortRange(range);
    }
  }

  public synchronized void addPortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException {
    addPortRange(new PortRange(name, min, max));
  }

  public synchronized void addPortRange(PortRange range) throws PortRangeInvalidException, PortRangeConflictException {
    if (range.getMax() < range.getMin()) {
      throw new PortRangeInvalidException("Max port must be greater than min port for: " + range);
    }
    if (store.containsRange(range.getName())) {
      throw new PortRangeConflictException("Port range already exists for: " + range.getName());
    }
    Iterator<PortRange> ranges = store.getPortRanges();
    while (ranges.hasNext()) {
      PortRange existing = (PortRange) ranges.next();
      if (existing.isConflicting(range)) {
        throw new PortRangeConflictException("Existing port range (" + existing.getName() + ") conflicting with new range");
      }
    }
    store.writeRange(range);
  }

  @Override
  public synchronized void updatePortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException {
    PortRange range = new PortRange(name, min, max);
    if (max < min) {
      throw new PortRangeInvalidException("Max port must be greater than min port for: " + range);
    }

    Iterator<PortRange> ranges = store.getPortRanges();
    while (ranges.hasNext()) {
      PortRange existing = (PortRange) ranges.next();

      if (!existing.getName().equals(range.getName()) && existing.isConflicting(range)) {
        throw new PortRangeConflictException("Existing port range (" + existing.getName() + ") conflicting with range");
      }
    }
    store.writeRange(range);
  }

  public synchronized void removePortRange(Arg name, boolean force) throws PortActiveException {
    Collection<PortRange> ranges = store.readRange(name);

    for (PortRange range : ranges) {
      if (range.hasBusyPorts()) {
        if (!force) {
          throw new PortActiveException("Range has ports for which processes are running");
        }
      }
    }
    for (PortRange range : ranges) {
      store.deleteRange(range.getName());
    }
  }

  public synchronized void releasePortRange(final Arg name) {
    Collection<PortRange> ranges = store.readRange(name);
    for (PortRange range : ranges) {
      range.releaseAll();
      store.writeRange(range);
    }
  }

  public synchronized List<PortRange> getPortRanges() {
    List<PortRange> lst = new ArrayList<PortRange>();
    Iterator<PortRange> ranges = store.getPortRanges();
    while (ranges.hasNext()) {
      PortRange range = (PortRange) ranges.next();
      lst.add(range);
    }
    Collections.sort(lst);
    return lst;
  }

  public String getRoleName() {
    return ROLE;
  }
}
