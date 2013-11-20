package org.sapia.corus.cron;

import org.apache.log.Hierarchy;
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
 * Implements the notion of "cron job" withing Corus.
 * 
 * @author Yanick Duchesne
 */
public class CronJob extends AbstractPersistent<String, CronJob> implements java.io.Serializable, AlarmListener {

  static final long serialVersionUID = 1L;

  private static final org.apache.log.Logger LOG = Hierarchy.getDefaultHierarchy().getLoggerFor(CronJob.class.getName());

  private transient CronModuleImpl owner;
  private transient ServerContext serverContext;
  private CronJobInfo info;

  CronJob() {
  }

  CronJob(CronJobInfo info) {
    this.info = info;
  }

  @Override
  @Transient
  public String getKey() {
    return info.getId();
  }

  void init(CronModuleImpl owner, ServerContext serverContext) {
    this.owner = owner;
    this.serverContext = serverContext;
  }

  /**
   * @return this instance's {@link ServerContext}
   */
  @Transient
  ServerContext getServerContext() {
    return serverContext;
  }

  /**
   * @return this instance's {@link CronModuleImpl}
   */
  @Transient
  public CronModuleImpl getOwner() {
    return owner;
  }

  /**
   * @return this instance's {@link CronJobInfo}
   */
  CronJobInfo getInfo() {
    return info;
  }

  /**
   * @see AlarmListener#handleAlarm(AlarmEntry)
   */
  public void handleAlarm(AlarmEntry entry) {
    try {
      Processor proc = serverContext.getServices().getProcessor();
      ProcessCriteria criteria = ProcessCriteria.builder().distribution(info.getDistribution()).version(info.getVersion()).profile(info.getProfile())
          .name(info.getProcessName()).build();
      ProgressQueue queue = proc.exec(criteria, 1);
      LOG.info("Executing scheduled VM " + info);
      ProgressQueueLogger.transferMessages(LOG, queue);
    } catch (Throwable t) {
      entry.isRepetitive = false;
      owner.removeCronJob(info.getId());
    }
  }
}
