package org.sapia.corus.client;

import org.sapia.ubik.util.Strings;

/**
 * Encapsulate auto-clustering configuration.
 * 
 * @author yduchesne
 *
 */
public class AutoClusterFlag {
  
  private ClusterInfo clusterInfo;
  
  private boolean all;
  
  private AutoClusterFlag(ClusterInfo info, boolean all) {
    this.clusterInfo = info;
    this.all         = all;
  }
  
  /**
   * @return the {@link ClusterInfo} wrapped by this instance.
   */
  public ClusterInfo getClusterInfo() {
    return clusterInfo;
  }
  
  /**
   * @return <code>true</code> if even those commands that support the -cluster option
   * but have not been marked as clustered should be.
   */
  public boolean isAll() {
    return all && clusterInfo.isClustered();
  }
  
  public static AutoClusterFlag notClustered() {
    return new AutoClusterFlag(ClusterInfo.notClustered(), false);
  }
  
  public static AutoClusterFlag forAll(ClusterInfo clusterInfo) {
    return new AutoClusterFlag(clusterInfo, true);
  }
  
  public static AutoClusterFlag forExplicit(ClusterInfo clusterInfo) {
    return new AutoClusterFlag(clusterInfo, false);
  }

  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "all", all, "clusterInfo", clusterInfo);
  }
}
