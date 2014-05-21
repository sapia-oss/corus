package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.Result;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * A factory of {@link Result} comparators.
 * 
 * @author yduchesne
 *
 */
public class ResultComparators {

  private ResultComparators() {
  }
  
  public static Comparator<Result<?>> forHostIp() {
    return new Comparator<Result<?>>() {
      private Comparator<CorusHost> c = HostComparators.forIp();
      @Override
      public int compare(Result<?> r1, Result<?> r2) {
        return c.compare(r1.getOrigin(), r2.getOrigin());
      }
    };
  }

  public static Comparator<Result<?>> forHostName() {
    return new Comparator<Result<?>>() {
      private Comparator<CorusHost> c = HostComparators.forName();
      @Override
      public int compare(Result<?> r1, Result<?> r2) {
        return c.compare(r1.getOrigin(), r2.getOrigin());
      }
    };
  }

  public static Comparator<Result<?>> forHostRole() {
    return new Comparator<Result<?>>() {
      private Comparator<CorusHost> c = HostComparators.forRole();
      @Override
      public int compare(Result<?> r1, Result<?> r2) {
        return c.compare(r1.getOrigin(), r2.getOrigin());
      }
    };
  }

}
