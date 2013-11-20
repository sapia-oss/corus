package org.sapia.corus.client.services.cron;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.cron.DuplicateScheduleException;
import org.sapia.corus.client.exceptions.cron.InvalidTimeException;
import org.sapia.corus.client.exceptions.processor.ProcessConfigurationNotFoundException;

/**
 * Specifies the behavior of the Cron module, which schedules processes.
 * 
 * @author Yanick Duchesne
 */
public interface CronModule extends java.rmi.Remote, Module {

  public static final String ROLE = CronModule.class.getName();

  /**
   * Adds a new Cron job configuration.
   * 
   * @param info
   *          a {@link CronJobInfo} corresponding to a Cron job to add.
   * @throws InvalidTimeException
   *           if the time that is specified is invalid.
   * @throws DuplicateScheduleException
   *           if a schedule already exists for the given job.
   * @throws ProcessConfigurationNotFoundException
   *           if no process matches the given job.
   * @throws CorusException
   *           if an undefined error occurs.
   */
  public void addCronJob(CronJobInfo info) throws InvalidTimeException, DuplicateScheduleException, ProcessConfigurationNotFoundException,
      CorusException;

  /**
   * Removes the Cron job with the given ID.
   * 
   * @param id
   *          the ID of the Cron job to remove.
   */
  public void removeCronJob(String id);

  /**
   * @return the {@link List} of {@link CronJobInfo} instances that this
   *         instance holds.
   */
  public List<CronJobInfo> listCronJobs();
}
