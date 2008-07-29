package org.sapia.corus.interop.client;

import org.sapia.corus.interop.*;
import org.sapia.corus.interop.soap.FaultException;

import java.io.IOException;

import java.util.*;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestProtocol implements InteropProtocol {
  static private int counter = 0;
  
  Log     log       = new StdoutLog();
  int     pollCount;
  int     statCount;
  boolean restart;
  boolean confirm;

  public void confirmShutdown() throws FaultException, IOException {
    confirm = true;
  }

  public List poll() throws FaultException, IOException {
    pollCount++;

    return generateAck("POLL");
  }

  public void restart() throws FaultException, IOException {
    restart = true;
  }

  public List sendStatus(Status stat) throws FaultException, IOException {
    statCount++;

    return generateAck("STATUS");
  }

  public List pollAndSendStatus(Status stat) throws FaultException, IOException {
    pollCount++;
    statCount++;

    return generateAck("POLL-STATUS");
  }

  public void setLog(Log log) {
    this.log = log;
  }
  
  private static List generateAck(String suffix) {
    ArrayList list = new ArrayList(1);
    Ack ack = new Ack();
    ack.setCommandId(++counter + suffix);
    list.add(ack);
    return list;
  }
}
