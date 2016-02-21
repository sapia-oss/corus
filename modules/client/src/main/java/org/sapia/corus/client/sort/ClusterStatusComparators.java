package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.cluster.ClusterStatus;

public class ClusterStatusComparators {

  private ClusterStatusComparators() {
  }
  
  public static Comparator<ClusterStatus> forHostName() {
    return new Comparator<ClusterStatus>() {
      @Override
      public int compare(ClusterStatus s1, ClusterStatus s2) {
        return HostComparators.forName().compare(s1.getHost(), s2.getHost());
      }
    };
  }
  
  public static Comparator<ClusterStatus> forHostIp() {
    return new Comparator<ClusterStatus>() {
      @Override
      public int compare(ClusterStatus s1, ClusterStatus s2) {
        return HostComparators.forIp().compare(s1.getHost(), s2.getHost());
      }
    };
  }
}
