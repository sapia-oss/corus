package org.sapia.corus.client.services.cron;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.cron.DuplicateScheduleException;
import org.sapia.corus.client.exceptions.cron.InvalidTimeException;
import org.sapia.corus.client.exceptions.processor.ProcessConfigurationNotFoundException;


/**
 * @author Yanick Duchesne
 */
public interface CronModule extends java.rmi.Remote, Module {
  public static final String ROLE = CronModule.class.getName();

  public void addCronJob(CronJobInfo info) throws 
    InvalidTimeException,
    DuplicateScheduleException, 
    ProcessConfigurationNotFoundException, 
    CorusException;

  public void removeCronJob(String id);

  public List<CronJobInfo> listCronJobs();
}
