package org.sapia.corus.client.rest.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.ProgressResult;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements behavior for dispatching commands over batches of Corus nodes, and returing the
 * corresponding {@link ProgressResult}s.
 * 
 * @author yduchesne
 *
 */
public abstract class AsyncProgressTaskSupport extends AsyncTaskSupport implements ProgressCapableTask {
  
  /**
   * Holds contextual data pertaining tothe execution of an {@link AsyncProgressTaskSupport} instance.
   * 
   * @author yduchesne
   *
   */
  public static class AsyncProgressTaskContext {
    
    private TimeValue timeout = TimeValue.createSeconds(60);
    private int batchSize, minHosts;
    private ConnectorPool  connectors;
    private ClusterInfo clusterInfo = ClusterInfo.notClustered();
    private int maxErrors;
    
    public AsyncProgressTaskContext clustered() {
      clusterInfo = ClusterInfo.clustered();
      return this;
    }
    
    public AsyncProgressTaskContext clustered(ClusterInfo targets) {
      clusterInfo = targets;
      return this;
    }
    
    public AsyncProgressTaskContext batchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }
    
    /**
     * @return the number of hosts per batch to deploy to.
     */
    public int getBatchSize() {
      Assertions.greaterOrEqual(batchSize, 0, "Batch size must be equal to or greater than 0. Got: %s", batchSize);
      return batchSize;
    }
  
    public AsyncProgressTaskContext timeout(TimeValue timeout) {
      this.timeout = timeout;
      return this;
    }
  
    /**
     * @return the {@link TimeValue} corresponding to the timeout to observe when getting
     * the next {@link ProgressResult} - see {@link #AsyncProgressTaskSupport#getNextResult()}
     */
    public TimeValue getTimeout() {
      Assertions.illegalState(timeout == null, "Timeout not set");
      return timeout;
    }
    
    public AsyncProgressTaskContext minHosts(int minHosts) {
      this.minHosts = minHosts;
      return this;
    }
    
    /**
     * @return the number of hosts that must be present in the cluster for the given batch size
     * (see {@link #getBatchSize()}) to be applied (otherwise, falls back to 1 host at a time).
     */
    public int getMinHosts() {
      Assertions.greater(batchSize, 0, "Min hosts must be greater than 0. Got: %s", minHosts);
      return minHosts;
    }
    
    public AsyncProgressTaskContext maxErrors(int max) {
      this.maxErrors = max;
      return this;
    }
    
    /**
     * @return the maximum number of host batches that are tolerated to result in an error.
     */
    public int getMaxErrors() {
      Assertions.greaterOrEqual(maxErrors, 0, "Max errorrs size must be equal to or greater than 0. Got: %s", maxErrors);
      return maxErrors;
    }
    
    public AsyncProgressTaskContext connectors(ConnectorPool connectors) {
      this.connectors = connectors;
      return this;
    }
    
    /**
     * @return the {@link ConnectorPool} to use.
     */
    public ConnectorPool getConnectors() {
      Assertions.illegalState(connectors == null, "Connector pool not set");
      return connectors;
    }
  }
  
  // ==========================================================================
  
  private BlockingQueue<ProgressResult> results = new LinkedBlockingDeque<ProgressResult>();
  
  private AsyncProgressTaskContext      context;

  private SysClock                      clock;
  
  private long                          taskStartTime;
  private List<Func<ProgressCapableTask, AsyncParams>> chainedTasks = new ArrayList<>();
  
  protected AsyncProgressTaskSupport(AsyncProgressTaskContext context) {
    this(context, RealtimeClock.getInstance());
  }

  protected AsyncProgressTaskSupport(AsyncProgressTaskContext context, SysClock clock) {
    this.context  = context;
    this.clock    = clock;
    taskStartTime = clock.currentTimeMillis();
  }
  
  public AsyncProgressTaskSupport addChainedTask(Func<ProgressCapableTask, AsyncParams> supplier) {
    this.chainedTasks.add(supplier);
    return this;
  }
  
  @Override
  public ProgressResult getNextResult() throws TimeoutException {
    ProgressResult r = results.peek();
    if (r == null) { 
      if (clock.currentTimeMillis() - taskStartTime > context.getTimeout().getValueInMillis()) {
        throw new TimeoutException(String.format("Not result available within prescribed delay of %s seconds", context.getTimeout().getValueInSeconds()));
      } else {
        r = new ProgressResult(Arrays.asList("Operation in process, progress information not yet available")).setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
      }
    } else {
      try {
        results.take();
      } catch (InterruptedException e) {
        throw new IllegalStateException("Thread interrupted", e);
      }
    }
    return r;
  }
  
  @Override
  public ProgressResult drainAllResults() {
    ProgressResult toReturn = new ProgressResult();
    if (results.peek() == null) {
      return new ProgressResult(Arrays.asList("Operation in process, progress information not yet available")).setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    }
    
    do {
      ProgressResult next = results.remove();
      toReturn.merge(next);
    } while (results.peek() != null && !toReturn.isError());
    
    return toReturn.isError() ? toReturn : toReturn.setStatus(HttpResponseFacade.STATUS_OK);
  }
  
  @Override
  protected void doTerminate() {
    ProgressResult lastResult = new ProgressResult(Arrays.asList("Task prematurely terminated"));
    results.add(lastResult.setStatus(HttpResponseFacade.STATUS_PARTIAL_CONTENT));    
  }

  /**
   * Empty implementation.
   */
  @Override
  protected void doExecute() {
    try {
      process();
    } catch (Exception e) {
      throw new IllegalStateException("Error occurred during execution", e);
    }
  }
 
  /**
   * Empty implementation.
   */
  @Override
  public void releaseResources() {
  }

  /**
   * @throws Exception if an arbitrary error occurs in the course of processing.
   */
  protected void process() throws Exception {
    CorusConnector connector = context.getConnectors().acquire();
    try {
      doProcess(connector);
    } finally {
      context.getConnectors().release(connector);
    }
  }
  
  private void doProcess(CorusConnector connector) {
    if (context.clusterInfo.getTargets().size() == 1) {
      doProcessNonClustered(connector);
    } else {
      doProcessClustered(connector);
    }
  }
  
  private void doProcessNonClustered(CorusConnector connector) {
    try {
      ProgressResult toAdd = doProcess(new AsyncParams(connector, context.clusterInfo));
      results.add(toAdd);
    } catch (RuntimeException e) {
      ProgressResult err = new ProgressResult(Arrays.asList("Error occurred while performing task"), e);
      results.add(err);
    }  
  }
  
  protected abstract ProgressResult doProcess(AsyncParams params);
  
  private void doProcessClustered(CorusConnector connector) {
    
    if (context.getBatchSize() > 0) {
      
      int globalErrorCount = 0;
      
      List<List<CorusHost>> batches = null;
      if (!context.clusterInfo.getTargets().isEmpty()) {
        List<CorusHost> allHosts = new ArrayList<CorusHost>();
        if (context.clusterInfo.getTargets().contains(connector.getContext().getServerHost())) {
          allHosts.add(connector.getContext().getServerHost());
        }
        
        for (CorusHost host : connector.getContext().getOtherHosts()) {
          if (context.clusterInfo.getTargets().contains(host)) {
            allHosts.add(host);
          }
        }

        batches = Collects.splitAsLists(allHosts, context.getMinHosts());
               
      } else {
        List<CorusHost> allHosts = new ArrayList<CorusHost>();
        allHosts.add(connector.getContext().getServerHost());
        allHosts.addAll(connector.getContext().getOtherHosts());
        batches = Collects.splitAsLists(allHosts, context.getMinHosts());
      }
      
      for (int i = 0; i < batches.size() && isRunning(); i++) {
        List<CorusHost> batch = batches.get(i);
        try {
          AsyncParams params = new AsyncParams(connector, ClusterInfo.clustered().addTargetHosts(batch));
          ProgressResult toAdd  = doProcess(params);
          toAdd.addProcessedHosts(batch);
          if (toAdd.isError()) {
            globalErrorCount++;
            assignErrorStatus(toAdd, context.getMaxErrors(), globalErrorCount, batches.size(), i);
            if (shouldContinue(toAdd, globalErrorCount)) {
              continue;
            } else {
              break;
            }
          } else {
            if (!chainedTasks.isEmpty()) {
              processChainedTasks(toAdd, params, batch);
              if (toAdd.isError()) {
                globalErrorCount++;
                assignErrorStatus(toAdd, context.getMaxErrors(), globalErrorCount, batches.size(), i);
                if (shouldContinue(toAdd, globalErrorCount)) {
                  continue;
                } else {
                  break;
                }
              } else {
                assignSuccessStatus(toAdd, context.getMaxErrors(), globalErrorCount, batches.size(), i);
                results.add(toAdd);
              }
            } else {
              assignSuccessStatus(toAdd, context.getMaxErrors(), globalErrorCount, batches.size(), i);
              results.add(toAdd);
            }
          }
        } catch (Exception e) {
          ProgressResult err = new ProgressResult(Arrays.asList("Error occurred while performing task"), e);
          err.addProcessedHosts(batch);
          globalErrorCount++;
          assignErrorStatus(err, context.getMaxErrors(), globalErrorCount, batches.size(), i);
          if (!shouldContinue(err, globalErrorCount)) {
             break;
          }
        }
      }
      
    } else {
      
      try {
        
        AsyncParams    params = new AsyncParams(connector, ClusterInfo.clustered());
        ProgressResult toAdd  = doProcess(params);

        if (toAdd.isError()) {
          results.add(toAdd);
        } else { 
          if (!chainedTasks.isEmpty()) {
            processChainedTasks(toAdd, params, new ArrayList<CorusHost>());
            results.add(toAdd);
          } else {
            results.add(toAdd);
          }
        }
      } catch (Exception e) {
        ProgressResult err = new ProgressResult(Arrays.asList("Error occurred while performing task"), e);
        results.add(err);
      }
      
    }
  }
  
  private void processChainedTasks(ProgressResult parent, AsyncParams params, List<CorusHost> batch) throws TimeoutException {
    for (Func<ProgressCapableTask, AsyncParams> supplier : chainedTasks) {
      ProgressCapableTask chainedTask = supplier.call(params);
      chainedTask.execute();
      ProgressResult chainedResult = chainedTask.getNextResult();
      parent.merge(chainedResult);
    }
  }
  
  static void assignSuccessStatus(ProgressResult result, int maxErrors, int currentErrorCount, int numberOfBatches, int currentBatchIndex) {
    int processedBatches = currentBatchIndex + 1;
    if (currentBatchIndex < numberOfBatches - 1) {
      result.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else if (processedBatches == currentErrorCount) {
      result.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR); 
    } else if (currentErrorCount > 0) {
      result.setStatus(HttpResponseFacade.STATUS_PARTIAL_SUCCESS);
    } else {
      result.setStatus(HttpResponseFacade.STATUS_OK);
    }
  }
  
  static void assignErrorStatus(ProgressResult result, int maxErrors, int currentErrorCount, int numberOfBatches, int currentBatchIndex) {
    int processedBatches = currentBatchIndex + 1;
    int successCount     = processedBatches - currentErrorCount;
    if (maxErrors > 0) {
      // if the error count is larger than the max error threshold, 
      // setting either partial success or server error status.
      if (currentErrorCount > maxErrors) {
      
        // if there are still batches left to process
        if (currentBatchIndex < numberOfBatches - 1) {
          result.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR);
        } 
        
        // if not all batches have resulted in an error: partial success
        else if (successCount > 0) {
          result.setStatus(HttpResponseFacade.STATUS_PARTIAL_SUCCESS);
        // if all batches have resulted in an error: server error
        } else {
          result.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
        }
        
      // batches are still left to process
      } else if (currentBatchIndex < numberOfBatches - 1) {
        result.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR);
        
      // no batches left to process
      } else {
        // if not all batches have resulted in an error: partial success
        if (successCount > 0) {
          result.setStatus(HttpResponseFacade.STATUS_PARTIAL_SUCCESS);
        // if all batches have resulted in an error: server error
        } else {
          result.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
        }
      }
    } else if (successCount > 0 ){
      result.setStatus(HttpResponseFacade.STATUS_PARTIAL_SUCCESS);
    } else {
      result.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    }    
  }
  
  boolean shouldContinue(ProgressResult result, int currentErrorCount) {
    results.add(result);
    if (context.getMaxErrors() > 0) {
      return currentErrorCount <= context.getMaxErrors();
    } else {
      return false;
    }
  }
}
