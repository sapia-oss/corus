package org.sapia.corus.client.sort;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.sort.Sorting.SortSwitch;
import org.sapia.ubik.util.Strings;

/**
 * Holds information about a sort switch, and the order in which sorting
 * is to be done (ascending or descending).
 * 
 * @author yduchesne
 *
 */
public class SortSwitchInfo {
  
  /**
   * A build of {@link SortSwitchInfo} instances.
   *
   */
  public static class Builder {
    
    private Set<SortSwitchInfo> switches = new HashSet<SortSwitchInfo>();
    
    /**
     * @param swt a {@link SortSwitch}.
     * @return this instance.
     */
    public Builder ascending(SortSwitch swt) {
      switches.add(new SortSwitchInfo(swt, true));
      return this;
    }
    
    /**
     * @param swt a {@link SortSwitch}.
     * @return this instance.
     */
    public Builder descending(SortSwitch swt) {
      switches.add(new SortSwitchInfo(swt, false));
      return this;
    }    
    
    /**
     * @return the array of {@link SortSwitchInfo} instances that was built.
     */
    public SortSwitchInfo[] build() {
      return switches.toArray(new SortSwitchInfo[switches.size()]);
    }
    
    /**
     * @return a new {@link Builder}.
     */
    public static Builder newInstance() {
      return new Builder();
    }
  }
  
  // ==========================================================================
  
  private SortSwitch swt;
  private boolean ascending = true;
  
  public SortSwitchInfo(SortSwitch swt, boolean ascending) {
    this.swt       = swt;
    this.ascending = ascending;
  }
  
  /**
   * @return the actual sort switch.
   */
  public SortSwitch getSwitch() {
    return swt;
  }
  
  /**
   * @return <code>true</code> if this instance indicates ascending order, <code>false</code>
   * if it indicates descending order.
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * @param ascendingComparator a {@link Comparator} that compares based on ascending order.
   * @return a {@link Comparator} that compares based on this instance's <code>ascending</code>
   * flag, wrapping the given comparator if need be.
   */
  public <T> Comparator<T> order(final Comparator<T> ascendingComparator) {
    if (!ascending) {
      return new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
          return -ascendingComparator.compare(o1, o2);
        }
      };
    }
    return ascendingComparator;
  }
  
  public String toLiteral() {
    return swt.value() + (ascending ? "" : ":d");
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SortSwitchInfo) {
      SortSwitchInfo other = (SortSwitchInfo) obj;
      return other.getSwitch() == swt;
    } 
    return false;
  }
  
  @Override
  public int hashCode() {
    return swt.hashCode();
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "switch", swt, "ascending", ascending);
  }

}
