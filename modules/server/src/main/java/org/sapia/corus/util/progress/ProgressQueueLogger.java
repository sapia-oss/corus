package org.sapia.corus.util.progress;

import java.util.List;

import org.apache.log.Logger;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;


/**
 * A utility class for transferring the content of a {@link ProgressQueue} to
 * a {@link Logger}.
 * 
 * @author Yanick Duchesne
 */
public class ProgressQueueLogger {
	
	/**
	 * Outputs the {@link ProgressMsg} instances in the given progress queue
	 * to the given {@link Logger}.
	 * 
	 * @param log the target {@link Logger}.
	 * @param queue the {@link ProgressQueue} to transfer from.
	 * 
	 * @throws Throwable 
	 */
  public static void transferMessages(Logger log, ProgressQueue queue)
                               throws Throwable {
    List<ProgressMsg> msgs;
    ProgressMsg 			msg;

    while (queue.hasNext()) {
      msgs = queue.next();

      for (int i = 0; i < msgs.size(); i++) {
        msg = msgs.get(i);
        transferMessage(log, msg);
      }
    }
  }
  
  private static void transferMessage(Logger log, ProgressMsg msg)
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
