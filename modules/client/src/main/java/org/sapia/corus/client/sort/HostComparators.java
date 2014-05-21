package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * A factory of {@link CorusHost} comparators.
 * 
 * @author yduchesne
 *
 */
public class HostComparators {

  private HostComparators() {
  }
  
  public static Comparator<CorusHost> forIp() {
    return new Comparator<CorusHost>() {
      @Override
      public int compare(CorusHost h1, CorusHost h2) {
        int c = h1.getEndpoint().getServerTcpAddress().getHost().compareTo(h2.getEndpoint().getServerTcpAddress().getHost());
        if (c == 0) {
          c = h1.getEndpoint().getServerTcpAddress().getPort() - h2.getEndpoint().getServerTcpAddress().getPort();
        }
        return c;
      }
    };
  }
  
  public static Comparator<CorusHost> forName() {
    return new Comparator<CorusHost>() {
      @Override
      public int compare(CorusHost h1, CorusHost h2) {
        return h1.getHostName().compareTo(h2.getHostName());
      }
    };
  }
  
  public static Comparator<CorusHost> forRole() {
    return new Comparator<CorusHost>() {
      @Override
      public int compare(CorusHost h1, CorusHost h2) {
        return h1.getRepoRole().name().compareTo(h2.getRepoRole().name());
      }
    }; 
  }
}
