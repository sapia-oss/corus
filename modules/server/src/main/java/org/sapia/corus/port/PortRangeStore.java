package org.sapia.corus.port;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * Provides {@link PortRange} persistience/retrieval logic around a
 * {@link DbMap}.
 * 
 * @author yduchesne
 */
public class PortRangeStore implements Dumpable {

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

  public Collection<PortRange> readRange(final ArgMatcher name) {
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
  
  public void archiveRanges(RevId revId) {
    ranges.clearArchive(revId);
    ranges.archive(revId, Collects.convertAsList(ranges.iterator(), new Func<String, PortRange>() {
      @Override
      public String call(PortRange arg) {
        return arg.getName();
      }
    }));
  }
  
  public void unarchiveRanges(RevId revId) {
    ranges.unarchive(revId);
  }

  public void clear() {
    ranges.clear();
  }
  
  @Override
  public void dump(JsonStream stream) {
    ranges.dump(stream);
  }
  
  @Override
  public void load(JsonInput dump) {
    ranges.load(dump);
  }
}
