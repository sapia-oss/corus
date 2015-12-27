package org.sapia.corus.client.rest.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.util.TimeValue;

/**
 * Checks that processes have been killed (blocks until the condition is fulfilled, or until 
 * the specified timeout occurs).
 * 
 * @author yduchesne
 *
 */
public class WaitForProcessesKilledTask extends AsyncTaskSupport implements ProgressCapableTask {
  
  private ProcessCriteria         criteria;
  private AsyncParams             params;
  private volatile ProgressResult result;
  private TimeValue               retryInterval = TimeValue.createSeconds(10);
  private TimeValue               timeout       = TimeValue.createSeconds(240);
  
  public WaitForProcessesKilledTask(ProcessCriteria criteria, AsyncParams params) {
    this.criteria = criteria;
    this.params   = params;
  
  }
  
  /**
   * @param interval the {@link TimeValue} to use as a retry interval.
   * @return this instance.
   */
  public WaitForProcessesKilledTask setRetryInterval(TimeValue interval) {
    retryInterval = interval;
    return this;
  }
  
  /**
   * @param timeout the amount of time within which processes should be killed.
   * @return this instance.
   */
  public WaitForProcessesKilledTask setTimeout(TimeValue timeout) {
    this.timeout = timeout;
    return this;
  }
   
  @Override
  public ProgressResult getNextResult() {
    if (result == null) {
      return new ProgressResult().addMessage("Kill operation in progress").setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      return result;
    }
  }
  
  @Override
  public ProgressResult drainAllResults() {
    return getNextResult();
  }
  
  @Override
  public void releaseResources() {
  }
  
  @Override
  protected void doTerminate() {
  }
  
  @Override
  protected void doExecute() {
     
    List<Process> flattenedResults = new ArrayList<>();
    Delay delay = new Delay(timeout.getValue(), timeout.getUnit());
    do {
     flattenedResults.clear();
     
     Results<List<Process>> results = params.getConnector()
          .getProcessorFacade().getProcesses(criteria, params.getClusterInfo());
      
      while (results.hasNext()) {
        Result<List<Process>> result = results.next();
        flattenedResults.addAll(result.getData());
      }
      
      if (flattenedResults.isEmpty()) {
        break;
      }
    
      try {
        Thread.sleep(retryInterval.getValueInMillis());
      } catch (InterruptedException e) {
        throw new ThreadInterruptedException();
      }
      
    } while (delay.isNotOver() && flattenedResults.isEmpty() && isRunning());

    if (flattenedResults.isEmpty()) {
      result = new ProgressResult(Arrays.asList("Process kill completed"));
    } else {
      result = new ProgressResult(Arrays.asList("Processes not kill within specified timeout")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    }
  }

}
