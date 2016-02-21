package org.sapia.corus.client.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.log.ExtendedLogCallback;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link PartitionService} interface.
 * 
 * @author yduchesne
 *
 */
public class PartitionServiceImpl implements PartitionService {
    
  private ConcurrentHashMap<String, PartitionSet> partitionSets = new ConcurrentHashMap<>();
  
  private ExtendedLogCallback log   = new ExtendedLogCallback.NullExtendedLogCallback();
  private SysClock    clock = RealtimeClock.getInstance();
  
  public void setLogCallback(ExtendedLogCallback log) {
    this.log = log;
  }
  
  public void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  @Override
  public PartitionSet createPartitionSet(
      int batchSize,
      List<ArgMatcher> tagIncludes, 
      List<ArgMatcher> tagExcludes,
      CorusConnector connector,
      TimeValue timeout) {
 
    List<CorusHost> hosts = new ArrayList<CorusHost>();
    
    if (!tagIncludes.isEmpty() || !tagExcludes.isEmpty()) {
      Set<Tag> hostTags = connector.getConfigFacade().getTags(ClusterInfo.notClustered()).next().getData();
      if (isIncluded(hostTags, tagIncludes) && !isExcluded(hostTags, tagExcludes)) {
        hosts.add(connector.getContext().getServerHost());
      } 
      ClusterInfo       targets = ClusterInfo.clustered().addTargetHosts(connector.getContext().getOtherHosts());
      Results<Set<Tag>> results = connector.getConfigFacade().getTags(targets);
      while (results.hasNext()) {
        Result<Set<Tag>> r = results.next();
        if (isIncluded(r.getData(), tagIncludes) && !isExcluded(r.getData(), tagExcludes)) {
          hosts.add(r.getOrigin());
        }
      }
    } else {
      hosts.add(connector.getContext().getServerHost());
      hosts.addAll(connector.getContext().getOtherHosts());
    }
    
    List<List<CorusHost>> batches    = Collects.splitAsLists(hosts, batchSize);
    List<Partition>       partitions = new ArrayList<>(batches.size());
    for (int i = 0; i < batches.size(); i++) {
      List<CorusHost> batch = batches.get(i);
      Partition p = new Partition(i, batch);
      partitions.add(p);
    }
    
    PartitionSet set = new PartitionSet(clock, UUID.randomUUID().toString(), timeout, batchSize, partitions);
    partitionSets.put(set.getId(), set);
    if (log.isInfoEnabled()) {
      log.info(String.format("Created partition set: %s", set.getId()));
    }
    return set;
  }
  
  @Override
  public PartitionSet getPartitionSet(String partitionSetId)
      throws IllegalArgumentException {
    PartitionSet ps = partitionSets.get(partitionSetId);
    if (ps == null) {
      String msg = String.format("No partition set found for: %s", partitionSetId);
      log.error(msg);
      for (Map.Entry<String, PartitionSet> e : partitionSets.entrySet()) {
        log.error(String.format("Got partitions set: %s = %s", e.getKey(), e.getValue()));
      }
      throw new IllegalArgumentException(msg);
    }
    ps.touch(clock);
    return ps;
  }
  
  @Override
  public void deletePartitionSet(String partitionSetId) {
    partitionSets.remove(partitionSetId);
  }

  public void flushStalePartitionSets() {
    List<String> toFlush = new ArrayList<String>();
    for (Map.Entry<String, PartitionSet> e : partitionSets.entrySet()) {
      if (clock.currentTimeMillis() - e.getValue().getLastAccess() > e.getValue().getTimeout().getValueInMillis()) {
        if (log.isInfoEnabled()) {
          log.info(String.format(
              "Flushing partition %s. Current time = %s, last access time = %s", 
              e.getKey(), 
              new Date(clock.currentTimeMillis()), 
              new Date(e.getValue().getTimeout().getValueInMillis()))
          );
        }
        toFlush.add(e.getKey());
      }
    }
    for (String id : toFlush) {
      partitionSets.remove(id);
    }
  }
  
  boolean isIncluded(Set<Tag> hostTags, List<ArgMatcher> includes) {
    if (includes.isEmpty()) {
      return true;
    }
    for (ArgMatcher m : includes) {
      for (Tag t : hostTags) {
        if (m.matches(t.getValue())) {
          return true;
        }
      }
    }
    return false;
  }
  
  boolean isExcluded(Set<Tag> hostTags, List<ArgMatcher> excludes) {
    if (excludes.isEmpty()) {
      return false;
    }
    for (ArgMatcher m : excludes) {
      for (Tag t : hostTags) {
        if (m.matches(t.getValue())) {
          return true;
        }
      }
    }
    return false;
  }
}
