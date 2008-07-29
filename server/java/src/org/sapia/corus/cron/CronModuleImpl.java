package org.sapia.corus.cron;

import fr.dyade.jdring.AlarmEntry;
import fr.dyade.jdring.AlarmManager;
import fr.dyade.jdring.PastDateException;

import org.sapia.corus.CorusException;
import org.sapia.corus.CorusRuntime;
import org.sapia.corus.LogicException;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.admin.CommandArgParser;
import org.sapia.corus.db.DbMap;
import org.sapia.corus.db.DbModule;
import org.sapia.corus.deployer.Deployer;
import org.sapia.corus.util.IDGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CronModuleImpl extends ModuleHelper implements CronModule {
  static CronModuleImpl instance;
  private DbMap         _jobs;
  private AlarmManager  _alarms = new AlarmManager();

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    instance = this;
    _jobs    = ((DbModule) env().lookup(DbModule.ROLE)).getDbMap("cron.jobs");
    initAlarms();
  }

  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    try{
      _alarms.removeAllAlarms();
    }catch(RuntimeException e){}
  }

  /*////////////////////////////////////////////////////////////////////
                        Module INTERFACE METHOD
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return CronModule.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                    CronModule INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.cron.CronModule#addCronJob(CronJobInfo)
   */
  public synchronized void addCronJob(CronJobInfo info)
                               throws InvalidTimeException, LogicException, 
                                      CorusException {
    if (_log.isInfoEnabled()) {
      _log.info("adding cron job: " + info);
    }

    Deployer dep = (Deployer) CorusRuntime.getCorus().lookup(Deployer.ROLE);

    if (!dep.getDistribution(CommandArgParser.parse(info.getDistribution()), 
        CommandArgParser.parse(info.getVersion())).containsProcess(info.getVmName())) {
      throw new LogicException("Invalid VM name: " + info.getVmName());
    }

    info.assignId(IDGenerator.makeId());

    CronJob job = info.toCronJob();

    try {
      AlarmEntry entry = new AlarmEntry(info.getMinute(), info.getHour(),
                                        info.getDayOfMonth(), info.getMonth(),
                                        info.getDayOfWeek(), info.getYear(), job);

      if (_alarms.containsAlarm(entry)) {
        throw new LogicException("A cron job with the same schedule is already present; change the schedule of the new cron job.");
      } else {
        _alarms.addAlarm(entry);
      }
    } catch (PastDateException e) {
      throw new InvalidTimeException(e.getMessage());
    }

    _jobs.put(info.getId(), job);
  }

  /**
   * @see org.sapia.corus.cron.CronModule#removeCronJob(String)
   */
  public synchronized void removeCronJob(String id) {
    _log.debug("removing cron job: " + id);
    _jobs.remove(id);
    _alarms.removeAllAlarms();
    _alarms = new AlarmManager();
    initAlarms();
  }

  /**
   * @see org.sapia.corus.cron.CronModule#listCronJobs()
   */
  public synchronized List listCronJobs() {
    List     infos = new ArrayList(10);
    Iterator itr = _jobs.values();
    CronJob  job;

    while (itr.hasNext()) {
      job = (CronJob) itr.next();
      infos.add(job.getInfo());
    }

    return infos;
  }

  private synchronized void initAlarms() {
    Iterator itr = _jobs.values();
    CronJob  job;

    while (itr.hasNext()) {
      job = (CronJob) itr.next();

      try {
        _alarms.addAlarm(job.getInfo().getMinute(), job.getInfo().getHour(),
                         job.getInfo().getDayOfMonth(),
                         job.getInfo().getMonth(),
                         job.getInfo().getDayOfWeek(), job.getInfo().getYear(),
                         job);
      } catch (PastDateException e) {
        _jobs.remove(job.getInfo().getId());
      }
    }
  }
}
