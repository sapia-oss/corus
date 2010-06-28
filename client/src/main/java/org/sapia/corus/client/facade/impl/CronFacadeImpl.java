package org.sapia.corus.client.facade.impl;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.exceptions.cron.DuplicateScheduleException;
import org.sapia.corus.client.exceptions.cron.InvalidTimeException;
import org.sapia.corus.client.exceptions.processor.ProcessConfigurationNotFoundException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CronFacade;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.corus.client.services.cron.CronModule;

public class CronFacadeImpl extends FacadeHelper<CronModule> implements CronFacade{
  
  public CronFacadeImpl(CorusConnectionContext context) {
    super(context, CronModule.class);
  }
  
  @Override
  public void addCronJon(CronJobInfo info) throws InvalidTimeException,
      DuplicateScheduleException, ProcessConfigurationNotFoundException,
      Exception {
    context.lookup(CronModule.class).addCronJob(info);
  }
  
  @Override
  public Results<List<CronJobInfo>> getCronJobs(ClusterInfo cluster) {
    Results<List<CronJobInfo>> results = new Results<List<CronJobInfo>>();
    proxy.listCronJobs();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public void removeCronJob(String id) {
    context.lookup(CronModule.class).removeCronJob(id);
  }

}
