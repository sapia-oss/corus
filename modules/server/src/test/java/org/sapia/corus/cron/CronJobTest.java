package org.sapia.corus.cron;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.cron.CronJobInfo;

public class CronJobTest {
  
  private CronJob job;
  
  @Before
  public void setUp() {
    CronJobInfo info = new CronJobInfo("dist", "profile", "version", "name");
    job = new CronJob(info);
    info.assignId("1");
    
  }
  
  @Test
  public void testJson() {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    job.toJson(stream, ContentLevel.DETAIL);
    CronJob copy = CronJob.fromJson(JsonObjectInput.newInstance(writer.toString()));

    assertEquals(job.getInfo().getId(), copy.getInfo().getId());
    assertEquals(job.getInfo().getDayOfMonth(), copy.getInfo().getDayOfMonth());
    assertEquals(job.getInfo().getDayOfWeek(), copy.getInfo().getDayOfWeek());
    assertEquals(job.getInfo().getDistribution(), copy.getInfo().getDistribution());
    assertEquals(job.getInfo().getHour(), copy.getInfo().getHour());
    assertEquals(job.getInfo().getMinute(), copy.getInfo().getMinute());
    assertEquals(job.getInfo().getMonth(), copy.getInfo().getMonth());
    assertEquals(job.getInfo().getProcessName(), copy.getInfo().getProcessName());
    assertEquals(job.getInfo().getProfile(), copy.getInfo().getProfile());
    assertEquals(job.getInfo().getVersion(), copy.getInfo().getVersion());
    assertEquals(job.getInfo().getYear(), copy.getInfo().getYear());
  }

  
  @Test
  public void testSerialization() {
    CronJob copy = (CronJob) SerializationUtils.deserialize(SerializationUtils.serialize(job));
    
    assertEquals(job.getInfo().getId(), copy.getInfo().getId());
    assertEquals(job.getInfo().getCreation().toString(), copy.getInfo().getCreation().toString());
    assertEquals(job.getInfo().getDayOfMonth(), copy.getInfo().getDayOfMonth());
    assertEquals(job.getInfo().getDayOfWeek(), copy.getInfo().getDayOfWeek());
    assertEquals(job.getInfo().getDistribution(), copy.getInfo().getDistribution());
    assertEquals(job.getInfo().getHour(), copy.getInfo().getHour());
    assertEquals(job.getInfo().getMinute(), copy.getInfo().getMinute());
    assertEquals(job.getInfo().getMonth(), copy.getInfo().getMonth());
    assertEquals(job.getInfo().getProcessName(), copy.getInfo().getProcessName());
    assertEquals(job.getInfo().getProfile(), copy.getInfo().getProfile());
    assertEquals(job.getInfo().getVersion(), copy.getInfo().getVersion());
    assertEquals(job.getInfo().getYear(), copy.getInfo().getYear());
  }
}
