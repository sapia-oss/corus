package org.sapia.corus.client.common;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

/**
 * Makes unique IDs based on time and internal counter.
 * 
 * @author Yanick Duchesne
 */
public class IDGenerator {
  private static int _count = 0;

  private static synchronized int increment() {
    if (_count > 99) {
      _count = 0;
    }
    _count++;
    return _count;
  }

  public static synchronized String makeId() {
    return "" + System.currentTimeMillis() + increment();
  }

  public static synchronized String makeIdFromDate() {
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
    System.out.println(makeId());

    System.out.println(makeIdFromDate());
  }
}
