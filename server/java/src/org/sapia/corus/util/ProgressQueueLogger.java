package org.sapia.corus.util;

import java.util.List;

import org.apache.log.Logger;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProgressQueueLogger {
  public static void transferMessages(Logger log, ProgressQueue queue)
                               throws Throwable {
    List        msgs;
    ProgressMsg msg;

    while (queue.hasNext()) {
      msgs = queue.next();

      for (int i = 0; i < msgs.size(); i++) {
        msg = (ProgressMsg) msgs.get(i);
        transferMessage(log, msg);
      }
    }
  }

  public static void transferMessage(Logger log, ProgressMsg msg)
                              throws Throwable {
    switch (msg.getStatus()) {
      case ProgressMsg.DEBUG:
      case ProgressMsg.VERBOSE:
        log.debug(msg.getMessage().toString());

        break;

      case ProgressMsg.INFO:
        log.info(msg.getMessage().toString());

        break;

      case ProgressMsg.WARNING:
        log.warn(msg.getMessage().toString());

        break;

      case ProgressMsg.ERROR:

        if (msg.isThrowable()) {
          Throwable t = msg.getError();
          log.error(t.getMessage(), t);
          throw t;
        } else {
          log.error(msg.getMessage().toString());
        }

        break;
    }
  }
}
