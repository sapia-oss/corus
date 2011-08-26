package org.sapia.corus.cron;


import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.util.progress.ProgressQueueLogger;

import fr.dyade.jdring.AlarmEntry;
import fr.dyade.jdring.AlarmListener;

/**
 * @author Yanick Duchesne
 */
public class CronJob extends AbstractPersistent<String, CronJob> implements java.io.Serializable, AlarmListener {

  static final long serialVersionUID = 1L;
  
  private transient CronModuleImpl _owner;
  private transient ServerContext _serverContext;
  private CronJobInfo _info;

  CronJob(){}
  
  CronJob(CronJobInfo info) {
    _info = info;
  }
  
  @Override
  @Transient
  public String getKey() {
    return _info.getId();
  }
  
  void init(CronModuleImpl owner, ServerContext ctx){
    _owner = owner;
    _serverContext = ctx;
  }
  
  /**
   * @return this instance's {@link ServerContext}
   */
  @Transient
  ServerContext getServerContext() {
    return _serverContext;
  }
  
  /**
   * @return this instance's {@link CronModuleImpl}
   */
  @Transient
  public CronModuleImpl getOwner() {
    return _owner;
  }

  /**
   * @return this instance's {@link CronJobInfo}
   */
  CronJobInfo getInfo() {
    return _info;
  }

  /**
   * @see AlarmListener#handleAlarm(AlarmEntry)
   */
  public void handleAlarm(AlarmEntry entry) {
    try {
      Processor     proc  = _serverContext.getServices().getProcessor();
      ProcessCriteria criteria = ProcessCriteria.builder()
        .distribution(_info.getDistribution())
        .version(_info.getVersion())
        .profile(_info.getProfile())
        .name(_info.getProcessName())
        .build();
      ProgressQueue queue = proc.exec(criteria, 1);
      _owner.logger().info("executing schedule VM " + _info);
      ProgressQueueLogger.transferMessages(_owner.logger(), queue);
    } catch (Throwable t) {
      entry.isRepetitive = false;
      _owner.removeCronJob(_info.getId());
    }
  }
}
