package org.sapia.corus.client.rest.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.util.Assertions;

/**
 * A {@link ProgressCapableTask} implementation that encapsulates other {@link ProgressCapableTask}s
 * in a composition.
 * 
 * @author yduchesne
 *
 */
public class CompositeProgressTask implements ProgressCapableTask {
  
  private List<ProgressCapableTask> sequence = new ArrayList<ProgressCapableTask>();
  
  private volatile int     currentTaskIndex = 0;
  private volatile boolean isRunning;
  
  public CompositeProgressTask addTask(ProgressCapableTask toAdd) {
    sequence.add(toAdd);
    return this;
  }
  
  @Override
  public void terminate() {
    if (isRunning) {
      sequence.get(currentTaskIndex).terminate();
      isRunning = false;
    }
  }
  
  @Override
  public void releaseResources() {
    sequence.get(currentTaskIndex).releaseResources();
  }
  
  @Override
  public boolean isRunning() {
    return isRunning;
  }
  
  @Override
  public void execute() {
    Assertions.illegalState(sequence.isEmpty(), "This instance has not tasks to execute");
    isRunning = true;
    sequence.get(currentTaskIndex).execute();
  }

  @Override
  public ProgressResult getNextResult() throws TimeoutException {
    Assertions.illegalState(sequence.isEmpty(), "This instance has no tasks to execute");
    ProgressResult result = sequence.get(currentTaskIndex).getNextResult();
    if (result.getStatus() == HttpResponseFacade.STATUS_OK  && currentTaskIndex < sequence.size() - 1) {
      sequence.get(currentTaskIndex++).releaseResources();
      result.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
      triggerNextTask(sequence.get(currentTaskIndex));
    }
    return result;
  }
  
  @Override
  public ProgressResult drainAllResults() {
    Assertions.illegalState(sequence.isEmpty(), "This instance has no tasks to execute");
    ProgressResult result = sequence.get(currentTaskIndex).drainAllResults();
    if (result.getStatus() == HttpResponseFacade.STATUS_OK  && currentTaskIndex < sequence.size() - 1) {
      sequence.get(currentTaskIndex++).releaseResources();
      result.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
      triggerNextTask(sequence.get(currentTaskIndex));
    }
    return result;
  }
  
  protected void triggerNextTask(final ProgressCapableTask nextTask) {
    Spawn.run(new Runnable() {
      @Override
      public void run() {
        nextTask.execute();
      }
    });
  }

}
