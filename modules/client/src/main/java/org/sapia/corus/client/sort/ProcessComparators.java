package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.processor.Process;

/**
 * A factory of {@link Process} comparators.
 * 
 * @author yduchesne
 *
 */
public class ProcessComparators {

  private ProcessComparators() {
  }
  
  public static Comparator<Process> forName() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getDistributionInfo().getProcessName().compareTo(p2.getDistributionInfo().getProcessName());
      }
    };
  }
  
  public static Comparator<Process> forDistribution() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getDistributionInfo().getName().compareTo(p2.getDistributionInfo().getName());
      }
    };
  }
  
  public static Comparator<Process> forVersion() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getDistributionInfo().getVersion().compareTo(p2.getDistributionInfo().getVersion());
      }
    };
  }
  
  public static Comparator<Process> forId() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getProcessID().compareTo(p2.getProcessID());
      }
    };
  }
  
  public static Comparator<Process> forOsPid() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getOsPid().compareTo(p2.getOsPid());
      }
    };
  }
  
  public static Comparator<Process> forProfile() {
    return new Comparator<Process>() {
      @Override
      public int compare(Process p1, Process p2) {
        return p1.getDistributionInfo().getProfile().compareTo(p2.getDistributionInfo().getProfile());
      }
    };
  }
}
