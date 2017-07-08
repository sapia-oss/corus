package org.sapia.corus.client.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formats/parses dates in a thread-safe way: an instance of this class can be called concurrently from
 * multiple threads, as the {@link SimpleDateFormat} class is not thread-safe.
 * 
 * @author yduchesne
 *
 */
public class ThreadSafeDateFormatter {
  
  private static final String ISO_8601_UTC_PATTERN = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";
  
  private static final ThreadLocal<SimpleDateFormat> FORMATTER = new ThreadLocal<SimpleDateFormat>();
  
  private String pattern;
  
  /**
   * @param pattern the pattern to use for parsing/formatting.
   */
  public ThreadSafeDateFormatter(String pattern) {
    this.pattern = pattern;
  }
  
  /**
   * Returns an instance of this class that supports formatting/parsing using the following pattern:
   * <pre>yyyy-MM-dd'T'hh:mm:ss.SSS'Z'</pre>
   * 
   * @return a new {@link ThreadSafeDateFormatter}.
   */
  public static ThreadSafeDateFormatter getIsoUtcInstance() {
    return new ThreadSafeDateFormatter(ISO_8601_UTC_PATTERN);
  }
  
  /**
   * @param dateString a date string to parse.
   * @return the {@link Date} resulting from the parsing operation.
   * @throws ParseException if the given string does not correspond to the pattern that this instance expects.
   */
  public Date parse(String dateString) throws ParseException {
    return format().parse(dateString);
  }
  
  /**
   * @param date a {@link Date}.
   * @return the string representation of the given date, according to this instance's pattern.
   */
  public String format(Date date) {
    return format().format(date);
  }
  
  private SimpleDateFormat format() {
    SimpleDateFormat fmt = FORMATTER.get();
    if (fmt == null) {
      fmt = new SimpleDateFormat(pattern);
      FORMATTER.set(fmt);
    }
    return fmt;
  }

}
