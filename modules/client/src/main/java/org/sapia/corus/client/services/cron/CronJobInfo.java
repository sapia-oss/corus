package org.sapia.corus.client.services.cron;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.common.Mappable;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.WriterJsonStream;

/**
 * Stores information about a Cron job.
 * 
 * @author Yanick Duchesne
 */
public class CronJobInfo implements Externalizable, JsonStreamable, Mappable {

  static final long serialVersionUID = 1L;
  
  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;

  private int classVersion = CURRENT_VERSION;

  public static final int UNDEFINED = -1;
  private String distName;
  private String profile;
  private String version;
  private String processName;
  private String id;
  private int minute     = UNDEFINED;
  private int hour       = UNDEFINED;
  private int dayOfMonth = UNDEFINED;
  private int month      = UNDEFINED;
  private int dayOfWeek  = UNDEFINED;
  private int year       = UNDEFINED;
  private Date creation;
  private Date lastRun;
  
  /**
   * Meant for externalization only.
   */
  public CronJobInfo() {
  }
  
  /**
   * Constructor for CronJobInfo.
   */
  public CronJobInfo(String dist, String profile, String version, String vmName) {
    distName = dist;
    this.profile = profile;
    this.version = version;
    processName = vmName;
  }
  
  @Override
  public void toJson(JsonStream stream) {
    stream.beginObject()
      .field("classVersion").value(classVersion)
      .field("id").value(id)
      .field("distribution").value(distName)
      .field("profile").value(profile)
      .field("version").value(version)
      .field("process").value(processName)
      .field("minute").value(minute)
      .field("hour").value(hour)
      .field("dayOfMonth").value(dayOfMonth)
      .field("month").value(month)
      .field("dayOfWeek").value(dayOfWeek)
      .field("year").value(year)
      .field("creationDate").value(creation)
      .field("lastRun").value(lastRun == null ? creation : lastRun)
    .endObject();
  }
  
  public static CronJobInfo fromJson(JsonInput in) {
    int inputVersion = in.getInt("classVersion");
    if (inputVersion == VERSION_1) {
      CronJobInfo cj = new CronJobInfo(
          in.getString("distribution"),
          in.getString("profile"),
          in.getString("version"),
          in.getString("process")
      );
      cj.id = in.getString("id");
      cj.setDayOfMonth(in.getInt("dayOfMonth"));
      cj.setDayOfWeek(in.getInt("dayOfWeek"));
      cj.setHour(in.getInt("hour"));
      cj.setMinute(in.getInt("minute"));
      cj.setMonth(in.getInt("month"));
      cj.setYear(in.getInt("year"));
      
      try {
        cj.creation = WriterJsonStream.parseDate(in.getString("creationDate"));
      } catch (ParseException e) {
        throw new IllegalStateException("Could not parse creation date", e);
      }
      
      try {
        cj.lastRun = WriterJsonStream.parseDate(in.getString("lastRun"));
      } catch (ParseException e) {
        throw new IllegalStateException("Could not parse last run date", e);
      }
      
      return cj;
    } else {
      throw new IllegalStateException("Unhandled version: " + inputVersion);
    }
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
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      distName    = in.readUTF();
      profile     = in.readUTF();
      version     = in.readUTF();
      processName = in.readUTF();
      id          = in.readUTF();
      minute      = in.readInt();
      hour        = in.readInt();
      dayOfMonth  = in.readInt();
      month       = in.readInt();
      dayOfWeek   = in.readInt();
      year        = in.readInt();
      creation    = (Date) in.readObject();
      lastRun     = (Date) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
    classVersion = CURRENT_VERSION;    
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(classVersion);
    out.writeUTF(distName);
    out.writeUTF(profile);
    out.writeUTF(version);
    out.writeUTF(processName);
    out.writeUTF(id);
    out.writeInt(minute);
    out.writeInt(hour);
    out.writeInt(dayOfMonth);
    out.writeInt(month);
    out.writeInt(dayOfWeek);
    out.writeInt(year);
    out.writeObject(creation);
    out.writeObject(lastRun);
  }

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("job.distribution", distName);
    toReturn.put("job.profile", profile);
    toReturn.put("job.version", version);
    toReturn.put("job.process", processName);
    toReturn.put("job.minute", minute);
    toReturn.put("job.hour", hour);
    toReturn.put("job.dayOfMonth", dayOfMonth);
    toReturn.put("job.month", month);
    toReturn.put("job.dayOfWeek", dayOfWeek);
    toReturn.put("job.year", year);
    toReturn.put("job.creationDate", creation == null ? new Date() : creation);
    toReturn.put("job.lastRunTime", lastRun == null ? (creation == null ? new Date() : creation) : lastRun);
    return toReturn;
  }
}
