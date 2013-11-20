package org.sapia.corus.client.services.cron;

import java.util.Date;

/**
 * Stores information about a Cron job.
 * 
 * @author Yanick Duchesne
 */
public class CronJobInfo implements java.io.Serializable {

  static final long serialVersionUID = 1L;

  public static final int UNDEFINED = -1;
  private String distName;
  private String profile;
  private String version;
  private String processName;
  private String id;
  private int minute = UNDEFINED;
  private int hour = UNDEFINED;
  private int dayOfMonth = UNDEFINED;
  private int month = UNDEFINED;
  private int dayOfWeek = UNDEFINED;
  private int year = UNDEFINED;
  private Date creation;
  private Date lastRun;

  /**
   * Constructor for CronJobInfo.
   */
  public CronJobInfo(String dist, String profile, String version, String vmName) {
    distName = dist;
    this.profile = profile;
    this.version = version;
    processName = vmName;
  }

  public String getId() {
    return id;
  }

  public String getDistribution() {
    return distName;
  }

  public String getProfile() {
    return profile;
  }

  public String getVersion() {
    return version;
  }

  public String getProcessName() {
    return processName;
  }

  public void setMinute(int min) {
    minute = min;
  }

  public int getMinute() {
    return minute;
  }

  public void setHour(int hour) {
    this.hour = hour;
  }

  public int getHour() {
    return hour;
  }

  public void setDayOfMonth(int dom) {
    dayOfMonth = dom;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getMonth() {
    return month;
  }

  public void setDayOfWeek(int dow) {
    dayOfWeek = dow;
  }

  public int getDayOfWeek() {
    return dayOfWeek;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getYear() {
    return year;
  }

  public Date getLastRunTime() {
    return lastRun;
  }

  public void touch() {
    lastRun = new Date();
  }

  public void assignId(String id) {
    this.id = id;
    creation = new Date();
  }

  public Date getCreation() {
    return creation;
  }
}
