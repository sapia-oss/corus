package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

/**
 * Signals that the Corus server has restarted a process.
 * 
 * @author yduchesne
 * 
 */
public class ProcessRestartedEvent extends CorusEventSupport {

  private Distribution  distribution;
  private ProcessConfig processConfig;
  private Process       process; 
  
  public ProcessRestartedEvent(Distribution dist, ProcessConfig processConfig, Process process) {
    this.distribution  = dist;
    this.processConfig = processConfig;
    this.process       = process;
  }

  public Distribution getDistribution() {
    return distribution;
  }

  public ProcessConfig getProcessConfig() {
    return processConfig;
  }
  
  public Process getProcess() {
    return process;
  }
  
  @Override
	public EventLevel getLevel() {
	  return EventLevel.INFO;
	}
  
  @Override
  public EventLog toEventLog() {
    return EventLog.builder()
    		.source(source())
    		.type(getClass())
    		.level(getLevel())
    		.message("Process %s was restarted", ToStringUtil.toString(process))
    		.build();
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("process");
    process.toJson(stream, ContentLevel.SUMMARY);
  }
  
  @Override
  protected Class<?> source() {
    return Processor.class;
  }

}
