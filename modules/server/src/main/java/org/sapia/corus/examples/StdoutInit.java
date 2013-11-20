package org.sapia.corus.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Yanick Duchesne
 *         <dl>
 *         <dt><b>Copyright:</b>
 *         <dd>Copyright &#169; 2002-2003 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All
 *         Rights Reserved.</dd></dt>
 *         <dt><b>License:</b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page</a> at the
 *         Sapia OSS web site</dd></dt>
 *         </dl>
 */
public class StdoutInit {
  public static void init() {
    try {
      File stdout = new File(System.getProperty("user.dir") + File.separator + "System.out_" + System.currentTimeMillis() + ".txt");
      File stderr = new File(System.getProperty("user.dir") + File.separator + "System.err_" + System.currentTimeMillis() + ".txt");
      PrintStream sysOut = new PrintStream(new FileOutputStream(stdout));
      PrintStream sysErr = new PrintStream(new FileOutputStream(stderr));
      System.setOut(sysOut);
      System.setErr(sysErr);
    } catch (IOException e) {
      // noop;
    }
  }
}
