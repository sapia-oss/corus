package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.port.PortRange;

public class PortRangeComparators {
  
  private PortRangeComparators() {
  }
  
  public static Comparator<PortRange> forName() {
    return new Comparator<PortRange>() {
      @Override
      public int compare(PortRange o1, PortRange o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
  }

}
