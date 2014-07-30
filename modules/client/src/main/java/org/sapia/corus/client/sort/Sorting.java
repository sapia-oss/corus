package org.sapia.corus.client.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.CompositeComparator;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.Process;

/**
 * Holds the constants for the different sort switches, and provides factory methods 
 * to create corresponding comparators.
 * 
 * @author yduchesne
 *
 */
public class Sorting {
  
  /**
   * Holds constants corresponding to the different sort switches.
   * 
   * @author yduchesne
   *
   */
  public enum SortSwitch {
    
    DIST_NAME("dn", "Distribution name"),
    DIST_VERSION("dv", "Distribution version"),
    PROC_NAME("pn", "Process name"),
    PROC_ID("pi", "Process ID"),
    PROC_OS_PID("po", "Operating system-assigned processs ID"),
    PROC_PROFILE("pp", "Process profile"),
    EXEC_CONFIG_NAME("en", "Execution configuration name"),
    EXEC_CONFIG_PROFILE("ep", "Execution configuration profile"),
    PORT_RANGE_NAME("rn", "Port range name"),    
    HOST_IP("hi", "Host IP"),
    HOST_NAME("hn", "Host name"),
    HOST_ROLE("hr", "Host role");
    
    private static Map<String, SortSwitch> SWITCH_BY_VALUE = new HashMap<String, Sorting.SortSwitch>();
    
    static {
      for (SortSwitch s : SortSwitch.values()) {
        SWITCH_BY_VALUE.put(s.value, s);
      }
    }
    
    private String value, description;
    
    private SortSwitch(String value, String description) {
      this.value = value;
      this.description = description;
    }
    
    /**
     * @return the actual switch used at the command line.
     */
    public String value() {
      return value;
    }
    
    /**
     * @return the switch description.
     */
    public String description() {
      return description;
    }
    
    public static SortSwitch forValue(String value) {
      return SWITCH_BY_VALUE.get(value);
    }
    
  }
  
  // ==========================================================================
  
  private Sorting() {
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link Process} instances.
   */
  public static final Comparator<Process> getProcessComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<Process> cmp = new CompositeComparator<Process>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.DIST_NAME) {
        cmp.add(s.order(ProcessComparators.forDistribution()));
      } else if (s.getSwitch() == SortSwitch.DIST_VERSION) {
        cmp.add(s.order(ProcessComparators.forVersion()));
      } else if (s.getSwitch() == SortSwitch.PROC_NAME) {
        cmp.add(s.order(ProcessComparators.forName()));
      } else if (s.getSwitch() == SortSwitch.PROC_ID) {
        cmp.add(s.order(ProcessComparators.forId()));
      } else if (s.getSwitch() == SortSwitch.PROC_OS_PID) {
        cmp.add(s.order(ProcessComparators.forOsPid()));
      } else if (s.getSwitch() == SortSwitch.PROC_PROFILE) {
        cmp.add(s.order(ProcessComparators.forProfile()));
      }
    }
    return cmp;
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link Distribution} instances.
   */
  public static final Comparator<Distribution> getDistributionComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<Distribution> cmp = new CompositeComparator<Distribution>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.DIST_NAME) {
        cmp.add(s.order(DistributionComparators.forName()));
      } else if (s.getSwitch() == SortSwitch.DIST_VERSION) {
        cmp.add(s.order(DistributionComparators.forVersion()));
      } 
    }
    return cmp;
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link CorusHost} instances.
   */
  public static final Comparator<CorusHost> getHostComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<CorusHost> cmp = new CompositeComparator<CorusHost>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.HOST_IP) {
        cmp.add(s.order(HostComparators.forIp()));
      } else if (s.getSwitch() == SortSwitch.HOST_NAME) {
        cmp.add(s.order(HostComparators.forName()));
      } else if (s.getSwitch() == SortSwitch.HOST_ROLE) {
        cmp.add(s.order(HostComparators.forRole()));
      }
    }
    return cmp;
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link ClusterStatus} instances.
   */
  public static final Comparator<ClusterStatus> getClusterStatusComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<ClusterStatus> cmp = new CompositeComparator<ClusterStatus>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.HOST_IP) {
        cmp.add(s.order(ClusterStatusComparators.forHostIp()));
      } else if (s.getSwitch() == SortSwitch.HOST_NAME) {
        cmp.add(s.order(ClusterStatusComparators.forHostName()));
      } else if (s.getSwitch() == SortSwitch.HOST_ROLE) {
        cmp.add(s.order(ClusterStatusComparators.forHostRole()));
      }
    }
    return cmp;   
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link ExecConfig} instances.
   */
  public static final Comparator<ExecConfig> getExecConfigComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<ExecConfig> cmp = new CompositeComparator<ExecConfig>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.EXEC_CONFIG_NAME) {
        cmp.add(s.order(ExecConfigComparators.forName()));
      } else if (s.getSwitch() == SortSwitch.EXEC_CONFIG_PROFILE) {
        cmp.add(s.order(ExecConfigComparators.forProfile()));
      } 
    }
    return cmp;   
  }
  
  /**
   * @param sortSwitches an array of sort switches.
   * @return a {@link Comparator} of {@link PortRange} instances.
   */
  public static final Comparator<PortRange> getPortRangeComparatorFor(SortSwitchInfo[] sortSwitches) {
    CompositeComparator<PortRange> cmp = new CompositeComparator<PortRange>();
    for (SortSwitchInfo s : sortSwitches) {
      if (s.getSwitch() == SortSwitch.PORT_RANGE_NAME) {
        cmp.add(s.order(PortRangeComparators.forName()));
      } 
    }
    return cmp;   
  }

  /**
   * Sorts a {@link Results} instance where each {@link Result} is expected to hold a single value.
   * 
   * @param toSort a {@link Results} instance to sort.
   * @param resultType the {@link Class} corresponding to the type of results held.
   * @param sortSwitches the {@link SortSwitchInfo} instances defining the sorting preferences.
   * @return the sorted {@link Result}s.
   */
  public static final <T> Results<T> sortSingle(Results<T> toSort, Class<T> resultType, SortSwitchInfo[] sortSwitches) {
    if (sortSwitches == null || sortSwitches.length == 0) {
      return toSort;
    }
    
    CompositeComparator<Result<?>> resultHostComparator = new CompositeComparator<Result<?>>();
    for (SortSwitchInfo s  : sortSwitches) {
      if (s.getSwitch() == SortSwitch.HOST_IP) {
        resultHostComparator.add(s.order(ResultComparators.forHostIp()));
      } else if (s.getSwitch() == SortSwitch.HOST_NAME) {
        resultHostComparator.add(s.order(ResultComparators.forHostName()));
      } else if (s.getSwitch() == SortSwitch.HOST_ROLE) {
        resultHostComparator.add(s.order(ResultComparators.forHostRole()));
      } 
    }
    
    List<Result<T>> listOfResults = new ArrayList<Result<T>>();
    while (toSort.hasNext()) {
      Result<T> r = toSort.next();
      listOfResults.add(r);
    }
    Collections.sort(listOfResults, resultHostComparator);
    Results<T> newResults = new Results<T>();
    newResults.setInvocationCount(listOfResults.size());
    for (Result<T> r : listOfResults) {
      newResults.addResult(r);
    }    
    return newResults;    
  }

  /**
   * Sorts a {@link Results} instance where each {@link Result} is expected to hold a {@link Set} of values.
   * 
   * @param toSort a {@link Results} instance to sort.
   * @param resultType the {@link Class} corresponding to the type of results held.
   * @param sortSwitches the {@link SortSwitchInfo} instances defining the sorting preferences.
   * @return the sorted {@link Result}s.
   */
  public static final <T> Results<List<T>> sortSet(Results<Set<T>> toSort, Class<T> resultType, SortSwitchInfo[] sortSwitches) {
    Results<List<T>> converted = new Results<List<T>>();
    int count = 0;
    while (toSort.hasNext()) {
      Result<Set<T>> r = toSort.next();
      converted.addResult(new Result<List<T>>(r.getOrigin(), new ArrayList<T>(r.getData()), r.getType()));
      count++;
    }
    converted.setInvocationCount(count);
    return sortList(converted, resultType, sortSwitches);
  }
  
  /**
   * Sorts a {@link Results} instance where each {@link Result} is expected to hold a {@link List} of values.
   * 
   * @param toSort a {@link Results} instance to sort.
   * @param resultType the {@link Class} corresponding to the type of results held.
   * @param sortSwitches the {@link SortSwitchInfo} instances defining the sorting preferences.
   * @return the sorted {@link Result}s.
   */
  @SuppressWarnings("unchecked")
  public static final <T> Results<List<T>> sortList(Results<List<T>> toSort, Class<T> resultType, SortSwitchInfo[] sortSwitches) {
    if (sortSwitches == null || sortSwitches.length == 0) {
      return toSort;
    }
    
    List<Result<List<T>>> listOfResults = new ArrayList<Result<List<T>>>();
    while (toSort.hasNext()) {
      Result<List<T>> r = toSort.next();
      listOfResults.add(r);
    }

    CompositeComparator<Result<?>> resultHostComparator = new CompositeComparator<Result<?>>();
    for (SortSwitchInfo s  : sortSwitches) {
      if (s.getSwitch() == SortSwitch.HOST_IP) {
        resultHostComparator.add(s.order(ResultComparators.forHostIp()));
      } else if (s.getSwitch() == SortSwitch.HOST_NAME) {
        resultHostComparator.add(s.order(ResultComparators.forHostName()));
      } else if (s.getSwitch() == SortSwitch.HOST_ROLE) {
        resultHostComparator.add(s.order(ResultComparators.forHostRole()));
      } 
    }
    
    if (resultHostComparator != null) {
      Collections.sort(listOfResults, resultHostComparator);
    }
 
    Comparator<T> itemComparator = null;
    if (resultType.equals(Process.class)) {
      itemComparator = (Comparator<T>) getProcessComparatorFor(sortSwitches);
    } else if (resultType.equals(Distribution.class)) {
      itemComparator = (Comparator<T>) getDistributionComparatorFor(sortSwitches);
    } else if (resultType.equals(ExecConfig.class)) {
      itemComparator = (Comparator<T>) getExecConfigComparatorFor(sortSwitches);
    } else if (resultType.equals(ClusterStatus.class)) {
      itemComparator = (Comparator<T>) getClusterStatusComparatorFor(sortSwitches);
    } else if (resultType.equals(PortRange.class)) {
      itemComparator = (Comparator<T>) getPortRangeComparatorFor(sortSwitches);
    }    
    if (itemComparator != null) {
      List<Result<List<T>>> listOfSortedResults = new ArrayList<Result<List<T>>>(listOfResults.size());
      for (Result<List<T>> r : listOfResults) {
        List<T> sortedList = new ArrayList<T>(r.getData());
        Collections.sort(sortedList, itemComparator);
        listOfSortedResults.add(new Result<List<T>>(r.getOrigin(), sortedList, r.getType()));
      }
      return getResultsFor(listOfSortedResults);
    } else {
      return getResultsFor(listOfResults);
    }
  }
  
  private static <T> Results<List<T>> getResultsFor(List<Result<List<T>>> listOfResults) {
    Results<List<T>> newResults = new Results<List<T>>();
    newResults.setInvocationCount(listOfResults.size());
    for (Result<List<T>> r : listOfResults) {
      newResults.addResult(r);
    }    
    return newResults;
  }
}
