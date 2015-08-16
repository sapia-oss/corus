package org.sapia.corus.client.services.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public class ExecConfigTest {

  @Test
  public void testGetProcesses() {
    ExecConfig conf = new ExecConfig();
    conf.setName("testConf");
    conf.createProcess();
    conf.createProcess();
    assertEquals(2, conf.getProcesses().size());
  }

  @Test
  public void testNewInstance() throws Exception {
    File file = new File("etc/test/testConf.xml");
    InputStream fis = new FileInputStream(file);
    try {
      ExecConfig.newInstance(fis);
    } finally {
      fis.close();
    }
  }

  @Test
  public void testRemoveAll() {
    ExecConfig conf = new ExecConfig();
    conf.setName("testConf");

    ProcessDef proc1 = conf.createProcess();
    proc1.setDistribution("dist1");
    proc1.setVersion("1.0");
    proc1.setName("proc1");
    proc1.setProfile("test");

    ProcessDef proc2 = conf.createProcess();
    proc2.setDistribution("dist2");
    proc2.setVersion("1.0");
    proc2.setName("proc2");
    proc2.setProfile("test");

    Distribution dist = new Distribution("dist1", "1.0");
    conf.removeAll(dist);

    assertFalse("Process definition should have been removed", conf.getProcesses().contains(proc1));
    assertTrue("Process definition should not have been removed", conf.getProcesses().contains(proc2));

  }

  @Test
  public void testCompareTo() {
    List<ExecConfig> confs = new ArrayList<ExecConfig>();

    ExecConfig conf1 = new ExecConfig();
    conf1.setName("conf1");

    ExecConfig conf2 = new ExecConfig();
    conf2.setName("conf2");

    ExecConfig conf3 = new ExecConfig();
    conf3.setName("aconf");

    confs.add(conf1);
    confs.add(conf2);
    confs.add(conf3);

    Collections.sort(confs);

    assertEquals(conf3, confs.get(0));
    assertEquals(conf1, confs.get(1));
    assertEquals(conf2, confs.get(2));
  }
  
  @Test
  public void testJson() {
    ExecConfig conf = config();
    
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    conf.toJson(stream, ContentLevel.DETAIL);
    
    ExecConfig copy = ExecConfig.fromJson(JsonObjectInput.newInstance(writer.toString()));
    
    assertEquals(conf.getName(), copy.getName());
    assertEquals(conf.getProfile(), copy.getProfile());
    assertEquals(conf.isEnabled(), copy.isEnabled());
    assertEquals(conf.isStartOnBoot(), copy.isStartOnBoot());
    assertEquals(conf.getProcesses().size(), copy.getProcesses().size());

    for (int i = 0; i < conf.getProcesses().size(); i++) {
      ProcessDef p1 = conf.getProcesses().get(i);
      ProcessDef p2 = conf.getProcesses().get(i);
      
      assertEquals(p1.getDist(), p2.getDist());
      assertEquals(p1.getInstances(), p2.getInstances());
      assertEquals(p1.getName(), p2.getName());
      assertEquals(p1.getProfile(), p2.getProfile());
      assertEquals(p1.getVersion(), p2.getVersion());
    }
  }
  
  @Test
  public void testJson_null_profile() {
    ExecConfig conf = config();
    conf.setProfile(null);
    
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    conf.toJson(stream, ContentLevel.DETAIL);
    
    ExecConfig copy = ExecConfig.fromJson(JsonObjectInput.newInstance(writer.toString()));
    
    assertNull(copy.getProfile());
  }
  
  @Test
  public void testSerialization() {
    ExecConfig conf = config();
    
    ExecConfig copy = (ExecConfig) SerializationUtils.deserialize(SerializationUtils.serialize(conf));
    
    assertEquals(conf.getName(), copy.getName());
    assertEquals(conf.getProfile(), copy.getProfile());
    assertEquals(conf.isEnabled(), copy.isEnabled());
    assertEquals(conf.isStartOnBoot(), copy.isStartOnBoot());
    assertEquals(conf.getProcesses().size(), copy.getProcesses().size());

    for (int i = 0; i < conf.getProcesses().size(); i++) {
      ProcessDef p1 = conf.getProcesses().get(i);
      ProcessDef p2 = conf.getProcesses().get(i);
      
      assertEquals(p1.getDist(), p2.getDist());
      assertEquals(p1.getInstances(), p2.getInstances());
      assertEquals(p1.getName(), p2.getName());
      assertEquals(p1.getProfile(), p2.getProfile());
      assertEquals(p1.getVersion(), p2.getVersion());
    }
  }

  private ExecConfig config() {
    ExecConfig conf = new ExecConfig();
    conf.setName("testConf");
    conf.setEnabled(true);
    conf.setStartOnBoot(true);
    conf.setProfile("test");

    ProcessDef proc1 = conf.createProcess();
    proc1.setDistribution("dist1");
    proc1.setVersion("1.0");
    proc1.setName("proc1");
    proc1.setProfile("test");

    ProcessDef proc2 = conf.createProcess();
    proc2.setDistribution("dist2");
    proc2.setVersion("1.0");
    proc2.setName("proc2");
    proc2.setProfile("test");

    return conf;
  }
}
