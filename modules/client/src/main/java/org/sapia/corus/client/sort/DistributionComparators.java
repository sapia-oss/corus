package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * A factory of {@link Distribution} comparators.
 * 
 * @author yduchesne
 *
 */
public class DistributionComparators {

  private DistributionComparators() {
  }
  
  public static Comparator<Distribution> forName() {
    return new Comparator<Distribution>() {
      @Override
      public int compare(Distribution d1, Distribution d2) {
        return d1.getName().compareTo(d2.getName());
      }
    };
  }
  
  public static Comparator<Distribution> forVersion() {
    return new Comparator<Distribution>() {
      @Override
      public int compare(Distribution d1, Distribution d2) {
        return d1.getVersion().compareTo(d2.getVersion());
      }
    };
  }
}
