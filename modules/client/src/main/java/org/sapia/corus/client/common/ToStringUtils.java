package org.sapia.corus.client.common;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Strings;

/**
 * Offers utility methods for the generation of strings, given certain types objects.
 * 
 * @author yduchesne
 *
 */
public class ToStringUtils {

  private ToStringUtils() {
  }
  
  /**
   * @param distribution a {@link Distribution}.
   * @return a new displayable {@link String}.
   */
  public static String toString(Distribution distribution) {
    return Strings.toString(
        "name", distribution.getName(), 
        "version", distribution.getVersion() 
    );
  }
  
  /**
   * @param distribution the {@link Distribution} to which the given {@link ProcessConfig} corresponds.
   * @param conf a {@link ProcessConfig}.
   * @return a new displayable {@link String}.
   */
  public static String toString(Distribution distribution, ProcessConfig conf) {
    return Strings.toString(
        "distribution", distribution.getName(), 
        "version", distribution.getVersion(), 
        "process", conf.getName()
    );
  }

  /**
   * @param process a {@link Process}.
   * @return a new displayable {@link String}.
   */
  public static String toString(Process process) {
    return Strings.toString(
        "distribution", process.getDistributionInfo().getName(),
        "version", process.getDistributionInfo().getVersion(),
        "profile", process.getDistributionInfo().getProfile(),
        "process", process.getDistributionInfo().getProcessName(),
        "pid", process.getProcessID(),
        "status", process.getStatus().name()
    );
  }
  
  /**
   * @param delim a {@link String} to use as a delimiter.
   * @param toJoin the objects whose string representation should be joined.
   * @return a new {@link String} consisting of a joint string representation.
   */
  public static String joinToString(String delim, Object...toJoin) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < toJoin.length; i++) {
      if (toJoin[i] == null) {
        sb.append("null");
      } else {
        sb.append(toJoin[i]);
      }
      if (i < toJoin.length - 1) {
        sb.append(delim).append(" ");
      }
    }
    return sb.toString();
  }
  
  /**
   * Creates a joint comma-delimited string representation of the given objects.
   * 
   * @param toJoin the objects whose string representation should be joined.
   * @return a new {@link String} consisting of a joint string representation.
   */
  public static String joinToString(Object...toJoin) {
    return joinToString(",", toJoin);
  }

  
  
}
