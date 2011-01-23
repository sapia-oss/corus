package org.sapia.corus.client.services.cron;

import java.util.Date;



/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CronJobInfo implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  public static final int UNDEFINED   = -1;
  private String          _distName;
  private String          _profile;
  private String          _version;
  private String          _processName;
  private String          _id;
  private int             _minute     = UNDEFINED;
  private int             _hour       = UNDEFINED;
  private int             _dayOfMonth = UNDEFINED;
  private int             _month      = UNDEFINED;
  private int             _dayOfWeek  = UNDEFINED;
  private int             _year       = UNDEFINED;
  private Date            _creation;
  private Date            _lastRun;

  /**
   * Constructor for CronJobInfo.
   */
  public CronJobInfo(String dist, String profile, String version, String vmName) {
    _distName = dist;
    _profile  = profile;
    _version  = version;
    _processName   = vmName;
  }

  public String getId() {
    return _id;
  }

  public String getDistribution() {
    return _distName;
  }

  public String getProfile() {
    return _profile;
  }

  public String getVersion() {
    return _version;
  }

  public String getProcessName() {
    return _processName;
  }

  public void setMinute(int min) {
    _minute = min;
  }

  public int getMinute() {
    return _minute;
  }

  public void setHour(int hour) {
    _hour = hour;
  }

  public int getHour() {
    return _hour;
  }

  public void setDayOfMonth(int dom) {
    _dayOfMonth = dom;
  }

  public int getDayOfMonth() {
    return _dayOfMonth;
  }

  public void setMonth(int month) {
    _month = month;
  }

  public int getMonth() {
    return _month;
  }

  public void setDayOfWeek(int dow) {
    _dayOfWeek = dow;
  }

  public int getDayOfWeek() {
    return _dayOfWeek;
  }

  public void setYear(int year) {
    _year = year;
  }

  public int getYear() {
    return _year;
  }

  public Date getLastRunTime() {
    return _lastRun;
  }

  public void touch() {
    _lastRun = new Date();
  }

  public void assignId(String id) {
    _id       = id;
    _creation = new Date();
  }
  
  public Date getCreation(){
    return _creation;
  }
}
