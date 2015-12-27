package org.sapia.corus.client.common;

import java.util.Collection;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * Offers utility methods for the generation of strings, given certain types objects.
 * 
 * @author yduchesne
 *
 */
public class ToStringUtil {

  private static final int    DEFAULT_ABBREVIATION_PADDING = 3;
  private static final String ABBREVIATION_DELIMITER       = "...";
  
  private ToStringUtil() {
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
        "starterType", process.getStarterType().name(),
        "status", process.getStatus().name()
    );
  }
 
  /**
   * @param image a {@link DockerImage}.
   * @return a new displaying {@link String}.
   */
  public static String toString(DockerImage image) {
    return Strings.toString(
        "id", abbreviate(image.getId(), 10), 
        "created", image.getCreationTimeStamp(), 
        "tags", image.getTags()
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
  public static <T> String joinToString(String delim, Collection<T> toJoin) {
    return joinToString(delim, toJoin.toArray(new Object[toJoin.size()]));
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

  /**
   * Creates a joint comma-delimited string representation of the given objects.
   * 
   * @param toJoin the objects whose string representation should be joined.
   * @return a new {@link String} consisting of a joint string representation.
   */
  public static <T> String joinToString(Collection<T> toJoin) {
    return joinToString(toJoin.toArray(new Object[toJoin.size()]));
  }

  /**
   * @param fields one more fields to concatenate.
   * @return a CSV-formatted string.
   */
  public static String joinAsCsv(Object...fields) {
    StringBuilder csv = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
     if (i > 0) {
       csv.append(",");
      }
      csv.append('"').append(fields[i] == null ? "" : fields[i]).append('"');
    }
    return csv.toString();
  }
  
  /**
   * Abbreviates a string for display.
   * 
   * @param toAbbreviate a string to abbreviate.
   * @param maxLen the maximum length that the string is expected to have.
   * @return the abbreviated string.
   */
  public static String abbreviate(String toAbbreviate, int maxLen) {
    return abbreviate(toAbbreviate, maxLen, DEFAULT_ABBREVIATION_PADDING, DEFAULT_ABBREVIATION_PADDING);
  } 
  
  /**
   * Abbreviates a string for display.
   * 
   * @param toAbbreviate a string to abbreviate.
   * @param maxLen the maximum length that the string is expected to have.
   * @param numStart the number of characters that should be kept at the beginning of the string.
   * @param numEnd the number of characters that should be kept at the end of the string.
   * @return the abbreviated string.
   */
  public static String abbreviate(String toAbbreviate, int maxLen, int numStart, int numEnd) {
    if (toAbbreviate == null || Strings.isBlank(toAbbreviate)) {
      return "";
    }
    Assertions.isTrue(numStart < maxLen, "Number of start characters must be smaller than max length");
    Assertions.isTrue(numEnd < maxLen, "Number of end characters must be smaller than max length");

    if (numStart + numEnd + ABBREVIATION_DELIMITER.length() > maxLen) {
      return toAbbreviate;
    } else if (toAbbreviate.length() > maxLen) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < numStart; i++) {
        sb.append(toAbbreviate.charAt(i));
      }
      sb.append(ABBREVIATION_DELIMITER);
      for (int i = toAbbreviate.length() - numEnd; i < toAbbreviate.length(); i++) {
        sb.append(toAbbreviate.charAt(i));
      }
      String abbreviated = sb.toString();
      Assertions.isTrue(
          abbreviated.length() <= maxLen, 
          "'%s' has an invalid length (%s). Must be equal to/smaller than specified max length (%)", 
          abbreviated, sb.length(), maxLen
      );
      return abbreviated;
    } else {
      return toAbbreviate;
    }
  }
}
