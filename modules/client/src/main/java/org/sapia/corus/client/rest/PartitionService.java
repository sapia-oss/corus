package org.sapia.corus.client.rest;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Allows subdividing a cluster into logical partitions, for performing operations on a subset
 * of the cluster at a time.
 * 
 * @author yduchesne
 */
public interface PartitionService {
  
  public static final TimeValue DEFAULT_PARTITION_SET_TIMEOUT = TimeValue.createSeconds(60 * 5);
  
  public class Partition implements JsonStreamable {
    
    private int index;
    private List<CorusHost> hosts;
    
    public Partition(int index, List<CorusHost> hosts) {
      this.index = index;
      this.hosts = hosts;
    }
 
    public int getIndex() {
      return index;
    }
    
    public List<CorusHost> getHosts() {
      return hosts;
    }
    
    public ClusterInfo getTargets() {
      return ClusterInfo.clustered().addTargetHosts(hosts);
    }
    
    @Override
    public void toJson(JsonStream stream, ContentLevel level) {
      stream.beginObject()
        .field("index").value(index)
        .field("hosts").beginArray();
      
      for (CorusHost host : hosts) {
        host.toJson(stream, level);
      } 
      
      stream.endArray();
      stream.endObject();
    }
  }
  
  // ==========================================================================
  
  /**
   * Models a {@link PartitionSet}.
   * 
   * @author yduchesne
   *
   */
  public class PartitionSet implements JsonStreamable {
    
    private String          id;
    private TimeValue       timeout;
    private int             partitionSize;
    private List<Partition> partitions;
    private long            lastAccess;
    
    public PartitionSet(String id, TimeValue timeout, int partitionSize, List<Partition> partitions) {
      this.id            = id;
      this.timeout       = timeout;
      this.partitionSize = partitionSize;
      this.partitions    = partitions;
    }
    
    /**
     * @return the ID of the partition set.
     */
    public String getId() {
      return id;
    }
    
    /**
     * @return the {@link TimeValue} to use as a timeout.
     */
    public TimeValue getTimeout() {
      return timeout;
    }
    
    /**
     * @return the partition size that was used to create this partition set.
     */
    public int getPartitionSize() {
      return partitionSize;
    }

    /**
     * @return the {@link List} of {@link Partition}s that this instance holds.
     */
    public List<Partition> getPartitions() {
      return partitions;
    }
    
    /**
     * @param index the index of the expected {@link Partition}.
     * @return the {@link Partition} corresponding to the given index.
     */
    public Partition getPartition(int index) {
      Assertions.greaterOrEqual(index, 0, "Invalid partition index: %. Expected >= 0", index);
      Assertions.lower(index, partitions.size(), "Invalid partition index: %s. Expected < %s", index, partitions.size());
      return partitions.get(index);
    }

    /**
     * @return the time (in millis) at which this instance was last accessed.
     */
    public long getLastAccess() {
      return lastAccess;
    }
    
    /**
     * @param clock the {@link SysClock} to use.
     */
    public void touch(SysClock clock) {
      lastAccess = clock.currentTimeMillis();
    }
    
    @Override
    public void toJson(JsonStream stream, ContentLevel level) {
      stream.beginObject()
        .field("id").value(id)
        .field("partitionSize").value(partitionSize)
        .field("partitions").beginArray();
      
      for (Partition p : partitions) {
        p.toJson(stream, level);
      }
      
      stream.endArray();
      stream.endObject();
    }
    
  }
  
  // ==========================================================================
  
  /**
   * @param partitionSize the number of nodes per partition.
   * @param the {@link List} of {@link ArgMatcher}s corresponding to included server tags.
   * @param the {@link List} of {@link ArgMatcher}s corresponding to excluded server tags.
   * @param connector the {@link CorusConnector} to use.
   * @param timeout the {@link TimeValue} corresponding to the timeout to use for the partition set
   * that will be created.
   * @return a new {@link PartitionSet}.
   */
  public PartitionSet createPartitionSet(
      int partitionSize, 
      List<ArgMatcher> tagIncludes, 
      List<ArgMatcher> tagExcludes, 
      CorusConnector connector,
      TimeValue timeout);
 
  /**
   * @param partitionSetId the ID of a partition set.
   * @return the {@link PartitionSet} that was found for the given ID.
   * @throws IllegalArgumentException if no such partition set was found.
   */
  public PartitionSet getPartitionSet(String partitionSetId) throws IllegalArgumentException;
  
  /**
   * @param partitionSetId the ID of the partition set to delete.
   */
  public void deletePartitionSet(String partitionSetId);

}
