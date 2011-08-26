package org.sapia.corus.deployer.task;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.file.FileSystemModule;

public class DeployTaskTest {
  
  private static final String TEST_RESOURCE = "testCorus.xml";
  
  TestServerContext ctx;

  @Before
  public void setUp() throws Exception {
    ctx = TestServerContext.create();
 
  }
  
  @Test
  public void testExecute() throws Exception{
    FileSystemModule fs = mock(FileSystemModule.class);
    when(fs.exists(any(File.class))).thenReturn(Boolean.FALSE);
    when(fs.openZipEntryStream(any(File.class), any(String.class)))
      .thenReturn(getCorusXmlStream());
    
    ctx.getServices().rebind(FileSystemModule.class, fs);
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, "testFile.zip").get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    assertTrue("Distribution was not deployed", ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
  }
  
  private InputStream getCorusXmlStream() throws IOException{
    InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE);
    if(is == null){
      throw new FileNotFoundException(TEST_RESOURCE);
    }
    return is;
  }

}
