package org.sapia.corus.cron;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log.Hierarchy;
import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
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
public class CronJob extends AbstractPersistent<String, CronJob> implements Externalizable, AlarmListener, JsonStreamable {

  static final long serialVersionUID = 1L;

  private static final org.apache.log.Logger LOG = Hierarchy.getDefaultHierarchy().getLoggerFor(CronJob.class.getName());

  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private transient CronModuleImpl owner;
  private transient ServerContext serverContext;
  private CronJobInfo info;

  /**
   * Meant for externalization only.
   */
  public CronJob() {
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
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    info.toJson(stream, level);
  }
  
  public static CronJob fromJson(JsonInput in) {
    CronJobInfo info = CronJobInfo.fromJson(in);
    return new CronJob(info);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      info = (CronJobInfo) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);
    out.writeObject(info);
  }
}
