package org.sapia.corus.ftest;

public class PartitionInfo {

  private String partitionSetId;
  private int    index;
  
  public PartitionInfo(String partitionSetId, int index) {
    this.partitionSetId = partitionSetId;
    this.index          = index;
  }
  
  public String getPartitionSetId() {
    return partitionSetId;
  }
  
  public int getIndex() {
    return index;
  }
  
  public String getResourcePart() {
    return "partitionsets/" + partitionSetId + "/partitions/" + index;
  }
  
  @Override
  public String toString() {
    return getResourcePart();
  }
}
