package org.sapia.corus.client.rest.async;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticStatus;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.util.TimeValue;

/**
 * Performs asynchronous diagnostic check.
 * 
 * @author yduchesne
 *
 */
public class PartialDiagnosticTask extends AsyncTaskSupport implements ProgressCapableTask {
  
  private AsyncParams             params;
  private volatile ProgressResult result;
  private TimeValue               retryInterval = TimeValue.createSeconds(10);
 
  public PartialDiagnosticTask(AsyncParams params) {
    this.params = params;
  }
  
  /**
   * @param interval the {@link TimeValue} to use as a retry interval.
   * @return this instance.
   */
  public PartialDiagnosticTask setRetryInterval(TimeValue interval) {
    retryInterval = interval;
    return this;
  }
   
  @Override
  public ProgressResult getNextResult() {
    if (result == null) {
      return new ProgressResult().addMessage("Diagnostic operation in progress").setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
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
    int errorCount;
    int pendingCount;
    final Reference<GlobalDiagnosticStatus> globalStatus = DefaultReference.of(GlobalDiagnosticStatus.SUCCESS);
    final List<PairTuple<CorusHost, GlobalDiagnosticResult>> flattenedResults = new ArrayList<>();
     
    do {
     errorCount   = 0;
     pendingCount = 0;
     flattenedResults.clear();
     
      Results<GlobalDiagnosticResult> results = params.getConnector()
          .getDiagnosticFacade()
          .acquireDiagnostics(params.getClusterInfo());
      
      while (results.hasNext()) {
        Result<GlobalDiagnosticResult> result = results.next();
        flattenedResults.add(new PairTuple<CorusHost, GlobalDiagnosticResult>(result.getOrigin(), result.getData()));
      }
      
      for (PairTuple<CorusHost, GlobalDiagnosticResult> r : flattenedResults) {
        if (r.getRight().getStatus() == GlobalDiagnosticStatus.FAILURE) {
          errorCount++;
          break;
        } else if (r.getRight().getStatus() == GlobalDiagnosticStatus.INCOMPLETE) {
          pendingCount++;
        }
      }
      
      if (errorCount > 0) {
        globalStatus.set(GlobalDiagnosticStatus.FAILURE);
      } else if (pendingCount > 0) {
        globalStatus.set(GlobalDiagnosticStatus.INCOMPLETE);
      } else {
        globalStatus.set(GlobalDiagnosticStatus.SUCCESS);
      }
      if (globalStatus.get() != GlobalDiagnosticStatus.INCOMPLETE) {
        break;
      } 
      try {
        Thread.sleep(retryInterval.getValueInMillis());
      } catch (InterruptedException e) {
        throw new ThreadInterruptedException();
      }
      
    } while (globalStatus.get() == GlobalDiagnosticStatus.INCOMPLETE && isRunning());
    
    JsonStreamable diagnosticPayload = new JsonStreamable() {
      public void toJson(JsonStream stream, ContentLevel level) {
        stream.field("diagnostic").beginObject()
          .field("status").value(globalStatus.get().name())
          .field("results").beginArray();
       
        for (PairTuple<CorusHost, GlobalDiagnosticResult> r : flattenedResults) {
          stream.beginObject()
            .field("cluster").value(params.getConnector().getContext().getDomain())
            .field("host").value(
              r.getLeft().getEndpoint().getServerTcpAddress().getHost() + ":" +
              r.getLeft().getEndpoint().getServerTcpAddress().getPort()
            )
            .field("dataType").value("diagnostic")
            .field("data");
          r.getRight().toJson(stream, level);
          stream.endObject();
        }
        stream.endArray().endObject();
      }
    };
    
    result = new ProgressResult();
    result.withNestedJson(diagnosticPayload);
    switch (globalStatus.get()) {
      case SUCCESS:
        result.setStatus(HttpResponseFacade.STATUS_OK);
        break;
      case FAILURE:
        result.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
        break;
      default:
        throw new IllegalStateException("Invalid status at this stage: " + globalStatus.get().name());
    }
  }

}
