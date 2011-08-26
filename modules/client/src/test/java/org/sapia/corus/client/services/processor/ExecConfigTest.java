package org.sapia.corus.client.services.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ExecConfigTest {
  
  @Test
  public void testGetProcesses() {
    ExecConfig conf = new ExecConfig();
    conf.setName("testConf");
    conf.createProcess();
    conf.createProcess();
    Assert.assertEquals(2, conf.getProcesses().size());
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
    
    Assert.assertEquals(conf3, confs.get(0));
    Assert.assertEquals(conf1, confs.get(1));    
    Assert.assertEquals(conf2, confs.get(2));
  }

}
