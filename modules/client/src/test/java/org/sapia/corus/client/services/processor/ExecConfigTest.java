package org.sapia.corus.client.services.processor;

import static org.junit.Assert.*;

import java.io.File;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import org.junit.Test;
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
  public void testNewInstance() throws Exception{
    File file = new File("etc/test/testConf.xml");
    InputStream fis = new FileInputStream(file);
    try{
      ExecConfig.newInstance(fis);
    }finally{
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
    assertTrue("Process definition should not have been removed" ,conf.getProcesses().contains(proc2));
    
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

}
