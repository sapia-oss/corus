package org.sapia.corus.cron;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.cron.DuplicateScheduleException;
import org.sapia.corus.client.exceptions.cron.InvalidTimeException;
import org.sapia.corus.client.exceptions.processor.ProcessConfigurationNotFoundException;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.corus.client.services.cron.CronModule;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

import fr.dyade.jdring.AlarmEntry;
import fr.dyade.jdring.AlarmManager;
import fr.dyade.jdring.PastDateException;

/**
 * This class implements the {@link CronModule} interface.
 * 
 * @author yduchesne
 */
@Bind(moduleInterface = CronModule.class)
@Remote(interfaces = { CronModule.class })
public class CronModuleImpl extends ModuleHelper implements CronModule {

  static CronModuleImpl instance;

  @Autowired
  private DbModule db;
  @Autowired
  private Deployer deployer;
  private DbMap<String, CronJob> jobs;
  private AlarmManager alarms = new AlarmManager();

  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    instance = this;
    jobs = db.getDbMap(String.class, CronJob.class, "cron.jobs");
    initAlarms();
  }

  /**
   * @see Service#dispose()
   */
  public void dispose() {
    try {
      alarms.removeAllAlarms();
    } catch (RuntimeException e) {
    }
  }

  /*
   * //////////////////////////////////////////////////////////////////// Module
   * INTERFACE METHOD
   * ////////////////////////////////////////////////////////////////////
   */

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return CronModule.ROLE;
  }

  /*
   * ////////////////////////////////////////////////////////////////////
   * CronModule INTERFACE METHODS
   * ////////////////////////////////////////////////////////////////////
   */

  /**
   * @see org.sapia.corus.client.services.cron.CronModule#addCronJob(CronJobInfo)
   */
  public synchronized void addCronJob(CronJobInfo info) throws InvalidTimeException, ProcessConfigurationNotFoundException, CorusException {
    if (log.isInfoEnabled()) {
      log.info("adding cron job: " + info);
    }

    if (!deployer.getDistribution(DistributionCriteria.builder().name(info.getDistribution()).version(info.getVersion()).build()).containsProcess(
        info.getProcessName())) {
      throw new ProcessConfigurationNotFoundException("Invalid process name: " + info.getProcessName());
    }

    info.assignId(IDGenerator.makeId());

    CronJob job = new CronJob(info);
    job.init(this, serverContext());

    try {
      AlarmEntry entry = new AlarmEntry(info.getMinute(), info.getHour(), info.getDayOfMonth(), info.getMonth(), info.getDayOfWeek(), info.getYear(),
          job);

      if (alarms.containsAlarm(entry)) {
        throw new DuplicateScheduleException("A cron job with the same schedule is already present; change the schedule of the new cron job");
      } else {
        alarms.addAlarm(entry);
      }
    } catch (PastDateException e) {
      throw new InvalidTimeException(e.getMessage());
    }

    jobs.put(info.getId(), job);
  }

  /**
   * @see org.sapia.corus.client.services.cron.CronModule#removeCronJob(String)
   */
  public synchronized void removeCronJob(String id) {
    log.debug("removing cron job: " + id);
    jobs.remove(id);
    alarms.removeAllAlarms();
    alarms = new AlarmManager();
    initAlarms();
  }

  public synchronized List<CronJobInfo> listCronJobs() {
    List<CronJobInfo> infos = new ArrayList<CronJobInfo>(10);
    Iterator<CronJob> itr = jobs.values();
    CronJob job;

    while (itr.hasNext()) {
      job = itr.next();
      infos.add(job.getInfo());
    }

    return infos;
  }

  private synchronized void initAlarms() {
    Iterator<CronJob> itr = jobs.values();
    CronJob job;

    while (itr.hasNext()) {
      job = (CronJob) itr.next();
      job.init(this, super.serverContext());
      try {
        alarms.addAlarm(job.getInfo().getMinute(), job.getInfo().getHour(), job.getInfo().getDayOfMonth(), job.getInfo().getMonth(), job.getInfo()
            .getDayOfWeek(), job.getInfo().getYear(), job);
      } catch (PastDateException e) {
        jobs.remove(job.getInfo().getId());
      }
    }
  }
}
