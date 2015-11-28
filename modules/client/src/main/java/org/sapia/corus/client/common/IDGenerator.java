package org.sapia.corus.client.common;

import java.util.Calendar;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

/**
 * Makes unique IDs based on time and internal counter.
 * 
 * @author Yanick Duchesne
 */
public class IDGenerator {

  private static final char[] ID_CHARS = new char[('z' - 'a') + 1 + ('Z' - 'A') + 1  + ('9' - '0') + 1];
  
  static {
    int charIndex = 0;
    for (int i = 0; i <= 'z' - 'a'; i++) {
      ID_CHARS[charIndex++] = (char) ('a' + i);
    }
    for (int i = 0; i <= 'Z' - 'A'; i++) {
      ID_CHARS[charIndex++] = (char) ('A' + i);
    }    
    for (int i = 0; i <= '9' - '0'; i++) {
      ID_CHARS[charIndex++] = (char) ('0' + i);
    }
  }
  
  private static final int MAX_COUNT = 99;
  
  private static int count = 0;

  private IDGenerator() {
  }
  
  private static synchronized int increment() {
    if (count > MAX_COUNT) {
      count = 0;
    }
    count++;
    return count;
  }

  /**
   * Makes sequential identifiers, using the current system time to which
   * an internal counter value is appended. The internal counter is reset when it 
   * reaches 99.
   * 
   * @return a new string consisting of a sequential identifier.
   */
  public static synchronized String makeSequentialId() {
    return "" + System.currentTimeMillis() + increment();
  }
  
  /**
   * Makes random string identifiers of given length, using characters with the 
   * [a - z], [A - Z] and [0 - 9] range inclusively.
   * 
   * @param length the number of characters expected in the output string.
   * @return a new {@link String}, composed of the given number of random characters.
   */
  public static synchronized String makeBase62Id(int length) {
    Random rand = new Random(System.currentTimeMillis());
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char c = ID_CHARS[rand.nextInt(ID_CHARS.length)];
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Makes date-based identifiers, of the format yyyyMMddsssss[counter], where
   * [counter] consists of an internal sequential counter which is reset when
   * it reaches 99. 
   * 
   * @return
   */
  public static synchronized String makeDateId() {
    Calendar cal = Calendar.getInstance();
    String year = Integer.toString(cal.get(Calendar.YEAR));
    if (year.charAt(1) != '0') {
      year = year.substring(year.length() - 3);
    } else {
      year = year.substring(year.length() - 2);
    }
    String month = StringUtils.leftPad(Integer.toString(cal.get(Calendar.MONTH) + 1), 2, '0');
    String day = StringUtils.leftPad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');

    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);

    long elapsedSeconds = (System.currentTimeMillis() - cal.getTimeInMillis()) / 1000;

    String counter = StringUtils.leftPad(Long.toString(elapsedSeconds), 5, '0');
    StringBuilder sb = new StringBuilder();
    sb.append(year).append(month).append(day).append(counter).append(Integer.toString(increment()));
    return sb.toString();
  }

  public static void main(String[] args) {
    System.out.println(makeSequentialId());

    System.out.println(makeDateId());
    System.out.println(makeBase62Id(32));
  }
}
