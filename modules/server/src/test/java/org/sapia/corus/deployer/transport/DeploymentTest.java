package org.sapia.corus.deployer.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;

/**
 * @author Yanick Duchesne
 */
public class DeploymentTest extends TestCase{

  /**
   * @param arg0
   */
  public DeploymentTest(String arg0) {
    super(arg0);
  }
  
  public void testGetContent() throws Exception{
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	byte[] data = new String("THIS IS DATA").getBytes();
  	DeploymentMetadata meta = new DeploymentMetadata("test", data.length, false);
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(meta);
    bos.write(data);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    Deployment depl = new Deployment(null, new TestConnection(bis, new ByteArrayOutputStream()));
    meta = depl.getMetadata();
    super.assertEquals("test", meta.getFileName());
		super.assertEquals(data.length, meta.getContentLength());    
    InputStream dataStream = depl.getConnection().getInputStream();
    dataStream.read(data);
    String dataStr = new String(data);
    super.assertEquals("THIS IS DATA", dataStr);
    
  }

}
