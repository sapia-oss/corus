package org.sapia.corus.port;

import java.util.List;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RevId;

import com.google.common.collect.Lists;

/**
 * Provides {@link PortRangeDefinition} persistience/retrieval logic around a
 * {@link DbMap}.
 * 
 * @author yduchesne
 */
public class PortRangeStore implements Dumpable {

  private DbMap<String, PortRangeDefinition> ranges;

  public PortRangeStore(DbMap<String, PortRangeDefinition> ranges) {
    this.ranges = ranges;
  }

  public List<PortRangeDefinition> getPortRanges() {
    return Lists.newArrayList(ranges.values());
  }

  public void writeRange(PortRangeDefinition range) {
    ranges.put(range.getName(), range);
  }

  public boolean containsRange(String name) {
    return ranges.get(name) != null;
  }

  public PortRangeDefinition readRange(String name) {
    return ranges.get(name);
  }

  public List<PortRangeDefinition> readRange(final ArgMatcher name) {
    return Lists.newArrayList(ranges.values()).stream().
        filter(p -> name.matches(p.getName())).
        sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).
        collect(Collectors.toList());
  }

  public void deleteRange(String name) {
    ranges.remove(name);
  }
  
  public void archiveRanges(RevId revId) {
    ranges.clearArchive(revId);
    ranges.archive(revId,
        Lists.newArrayList(ranges.values()).stream().
            map(PortRangeDefinition::getName).
            collect(Collectors.toList()));
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
