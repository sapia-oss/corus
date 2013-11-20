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
 * Provides {@link PortRange} persistience/retrieval logic around a
 * {@link DbMap}.
 * 
 * @author yduchesne
 */
public class PortRangeStore {

  private DbMap<String, PortRange> ranges;

  public PortRangeStore(DbMap<String, PortRange> ranges) {
    this.ranges = ranges;
  }

  public Iterator<PortRange> getPortRanges() {
    return ranges.values();
  }

  public void writeRange(PortRange range) {
    ranges.put(range.getName(), range);
  }

  public boolean containsRange(String name) {
    return ranges.get(name) != null;
  }

  public PortRange readRange(String name) {
    return ranges.get(name);
  }

  public Collection<PortRange> readRange(final Arg name) {
    return new IteratorFilter<PortRange>(new Matcher<PortRange>() {
      @Override
      public boolean matches(PortRange range) {
        return name.matches(range.getName());
      }
    }).filter(ranges.values()).sort(new Comparator<PortRange>() {

      @Override
      public int compare(PortRange o1, PortRange o2) {
        return o1.getName().compareTo(o2.getName());
      }
    }).get();
  }

  public void deleteRange(String name) {
    ranges.remove(name);
  }

  public void clear() {
    ranges.clear();
  }
}
