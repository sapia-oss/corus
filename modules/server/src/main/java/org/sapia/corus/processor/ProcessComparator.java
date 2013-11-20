package org.sapia.corus.processor;

import java.util.Comparator;

import org.sapia.corus.client.services.processor.Process;

/**
 * An instance of this class compares {@link Process} instances. It performs
 * comparison on the following fields, in the given order:
 * 
 * <ol>
 * <li>distribution
 * <li>version
 * <li>process name
 * <li>profile
 * <li>process ID
 * </ol>
 * 
 * @author yduchesne
 * 
 */
public class ProcessComparator implements Comparator<Process> {

  @Override
  public int compare(Process o1, Process o2) {
    int i = o1.getDistributionInfo().getName().compareTo(o2.getDistributionInfo().getName());
    if (i == 0) {
      return o1.getDistributionInfo().getVersion().compareTo(o2.getDistributionInfo().getVersion());
    }
    if (i == 0) {
      return o1.getDistributionInfo().getProcessName().compareTo(o2.getDistributionInfo().getProcessName());
    }
    if (i == 0) {
      return o1.getDistributionInfo().getProfile().compareTo(o2.getDistributionInfo().getProfile());
    }
    if (i == 0) {
      return o1.getProcessID().compareTo(o2.getProcessID());
    }

    return i;
  }

}
