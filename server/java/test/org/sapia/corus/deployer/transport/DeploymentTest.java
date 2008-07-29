package org.sapia.corus.deployer.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import org.sapia.corus.deployer.DeploymentMetadata;

import junit.framework.TestCase;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
  	DeploymentMetadata meta = new DeploymentMetadata("test", data.length, new HashSet(), false);
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(meta);
    bos.write(data);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    Deployment depl = new Deployment(new TestConnection(bis, new ByteArrayOutputStream()));
    meta = depl.getMetadata();
    super.assertEquals("test", meta.getFileName());
		super.assertEquals(data.length, meta.getContentLength());    
    InputStream dataStream = depl.getConnection().getInputStream();
    dataStream.read(data);
    String dataStr = new String(data);
    super.assertEquals("THIS IS DATA", dataStr);
    
  }

}
