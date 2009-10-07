package org.sapia.corus.cron;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.admin.ArgFactory;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueLogger;

import fr.dyade.jdring.AlarmEntry;
import fr.dyade.jdring.AlarmListener;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CronJob implements java.io.Serializable, AlarmListener {
  private CronJobInfo _info;

  CronJob(CronJobInfo info) {
    _info = info;
  }

  CronJobInfo getInfo() {
    return _info;
  }

  /**
   * @see com.jalios.jdring.AlarmListener#handleAlarm(AlarmEntry)
   */
  public void handleAlarm(AlarmEntry entry) {
    try {
      Processor     proc  = (Processor) CorusRuntime.getCorus().lookup(Processor.ROLE);
      ProgressQueue queue = proc.exec(ArgFactory.parse(_info.getDistribution()),
                                      ArgFactory.parse(_info.getVersion()), _info.getProfile(),
                                      ArgFactory.parse(_info.getVmName()), 1);
      CronModuleImpl.instance.logger().info("executing schedule VM " + _info);
      ProgressQueueLogger.transferMessages(CronModuleImpl.instance.logger(), queue);
    } catch (Throwable t) {
      entry.isRepetitive = false;
      CronModuleImpl.instance.removeCronJob(_info.getId());
    }
  }
}
