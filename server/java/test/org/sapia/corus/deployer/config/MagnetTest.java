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
public class MagnetTest extends TestCase {
  static final String CR      = System.getProperty("line.separator");
  static final String CONTENT = "<distribution name=\"test\" version=\"1.0\">" +
                                CR + "  <process name=\"httpServer\"" + CR +
                                "           maxKillRetry=\"3\"" + CR +
                                "           shutdownTimeout=\"30000\"" + CR +
                                "           invoke=\"true\">" + CR +
                                "    <magnet javaHome=\"/opt/jdk1.4.1\"" + CR +
                                "            corusHome=\"" + System.getProperty("corus.home", System.getProperty("user.dir")) + "\"" + CR +
                                "            profile=\"profile1\"" + CR +
                                "            magnetFile=\"magnet.xml\">" + CR +
                                "      <property name=\"some.property\" value=\"this is a VM property\" />" +
                                CR +
                                "      <xoption  name=\"ms\" value=\"128M\" />" +
                                CR + "    </magnet>" + CR + "  </process>" +
                                CR + "</distribution>";

  public MagnetTest(String arg0) {
    super(arg0);
  }

  public void testNewInstance() throws Exception {
    ByteArrayInputStream is     = new ByteArrayInputStream(CONTENT.getBytes());
    Distribution         d      = Distribution.newInstance(is);
    ProcessConfig        cfg    = d.getProcess("httpServer");
    CmdLine              cmd    = cfg.toCmdLine(new TestEnv("profile1"));
    String               cmdStr = cmd.toString().replace('\\', '/');
    System.out.println(cmdStr);
    super.assertTrue(cfg.toCmdLine(new TestEnv("none")) == null);
  }
}
