package org.sapia.corus.deployer;

import java.io.IOException;

import org.sapia.corus.util.ProgressQueue;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DeployOutputStream extends java.rmi.Remote {
  void close() throws IOException;

  void flush() throws IOException;

  void write(byte[] b) throws IOException;

  void write(byte[] b, int off, int len) throws IOException;

  void write(int b) throws IOException;

  ProgressQueue getProgressQueue();
}
