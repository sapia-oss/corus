package org.sapia.corus.deployer.config;

import junit.framework.TestCase;

import org.sapia.console.CmdLine;

import java.io.ByteArrayInputStream;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JavaTest extends TestCase {
  static final String CR      = System.getProperty("line.separator");
  static final String CONTENT = "<distribution name=\"test\" version=\"1.0\">" +
                                CR + "  <process name=\"httpServer\"" + CR +
                                "           maxKillRetry=\"3\"" + CR +
                                "           shutdownTimeout=\"30000\"" + CR +
                                "           invoke=\"true\">" + CR +
                                "    <java mainClass=\"org.acme.HttpServer\"" +
                                CR +
                                "          cp=\"${user.dir}/lib/httpserver.jar\"" +
                                CR + "          javaHome=\"" + System.getProperty("java.home") + "\"" +
                                CR + "          javaCmd=\"java\"" + CR +
                                "          profile=\"profile1\"" + CR +
                                "          args=\"-port 8080 -config ${user.dir}/config.xml\">" +
                                CR +
                                "      <property name=\"some.property\" value=\"this is a VM property\" />" +
                                CR +
                                "      <xoption  name=\"ms\" value=\"128M\" />" +
                                CR + "    </java>" + CR + "  </process>" + CR +
                                "</distribution>";

  public JavaTest(String arg0) {
    super(arg0);
  }

  public void testNewInstance() throws Exception {
    ByteArrayInputStream is     = new ByteArrayInputStream(CONTENT.getBytes());
    Distribution         d      = Distribution.newInstance(is);
    ProcessConfig        cfg    = d.getProcess("httpServer");
    CmdLine              cmd    = cfg.toCmdLine(new TestEnv("profile1"));
    String               cmdStr = cmd.toString().replace('\\', '/');
//    super.assertEquals("\"/opt/jdk1.4.1/bin/java\" -Xms128M -Dsome.property=\"this is a VM property\" -Duser.dir=\"common.dir\" -cp D:/dev/sapia/java/corus/lib/sapia_corus_starter.jar;D:/dev/sapia/java/corus/dist/sapia_corus_starter.jar;\"common.dir\"/lib/httpserver.jar org.acme.HttpServer -port 8080 -config common.dir/config.xml",
//S                       cmdStr);
    super.assertTrue(cfg.toCmdLine(new TestEnv("none")) == null);
  }
}
