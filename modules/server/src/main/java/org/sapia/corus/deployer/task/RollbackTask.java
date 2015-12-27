package org.sapia.corus.deployer.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.cli.EmbeddedInterpreter;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.StrLookups;
import org.sapia.corus.client.exceptions.deployer.RollbackScriptNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Status;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Type;
import org.sapia.corus.client.services.deployer.event.RollbackStartingEvent;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.deployer.DeployerThrottleKeys;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * Executes the rollback of a specific distribution.
 * 
 * @author yduchesne
 *
 */
public class RollbackTask extends Task<Void, TaskParams<Distribution, Void, Void, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return DeployerThrottleKeys.ROLLBACK_DISTRIBUTION;
  }
  
  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<Distribution, Void, Void, Void> params) throws Throwable {
    doExecute(ctx, params);
    return null;
  }
  
  private void doExecute(TaskExecutionContext ctx, TaskParams<Distribution, Void, Void, Void> params) throws Throwable {
    Distribution     dist      = params.getParam1();
    FileSystemModule fs        = ctx.getServerContext().getServices().getFileSystem();
    Processor        processor = ctx.getServerContext().getServices().getProcessor(); 
    Deployer         deployer  = ctx.getServerContext().getServices().getDeployer();  
    
    FilePath scriptPath = FilePath.newInstance()
          .addDir(dist.getCommonDir())
          .addDir("META-INF")
          .addDir("scripts")
          .setRelativeFile("rollback.corus");
    
    File scriptBaseDir = fs.getFileHandle(dist.getCommonDir());
    File scriptFile    = fs.getFileHandle(scriptPath.createFilePath()); 
    
    if (!scriptFile.exists()) {
      throw new RollbackScriptNotFoundException("Could not find rollback script for distribution: " + dist.getDislayInfo());
    }
    
    try {
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new RollbackStartingEvent(dist));
      doRunScript(fs, scriptBaseDir, scriptFile, dist, ctx);
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new RollbackCompletedEvent(dist, Type.USER, Status.SUCCESS));
    } catch (Exception e) {
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new RollbackCompletedEvent(dist, Type.USER, Status.FAILURE));
      ctx.error("Error caught while executing rollback script", e);
      throw e;
    }
    
    DistributionCriteria criteria = DistributionCriteria.builder().name(dist.getName()).version(dist.getVersion()).build();
    List<Distribution> dists = deployer.getDistributions(criteria);
    if (!dists.isEmpty()) {
      List<Process> processes = processor.getProcesses(
          ProcessCriteria.builder().distribution(dist.getName()).version(dist.getVersion()).build()
      );
      if (!processes.isEmpty()) {
        throw new RunningProcessesException("Processes still running for distribution: " + dist);
      } else {
        ctx.getTaskManager().executeAndWait(new UndeployTask(), TaskParams.createFor(criteria)).get();
      }     
    }
  }
  
  private void doRunScript(FileSystemModule fs, File scriptBaseDir, File scriptFile, Distribution dist, final TaskExecutionContext ctx) 
      throws FileNotFoundException, IllegalStateException {
    
    ctx.info("Executing rollback.corus script");
    
    EmbeddedInterpreter interpreter = new EmbeddedInterpreter(ctx.getServerContext().getCorus(), scriptBaseDir);
    
    interpreter.setOut(new ConsoleOutput() {
      @Override
      public void println(String s) {
        ctx.info(s);
      }
      
      @Override
      public void println() {
      }
      
      @Override
      public void print(char c) {
      }
      
      @Override
      public void print(String s) {
      }
      
      @Override
      public void flush() {
      }
    });
    
    // commands executed in this context must not be clustered.
    interpreter.setAutoClusterInfo(AutoClusterFlag.notClustered());
    Reader              scriptReader = null;
    try {
      scriptReader = fs.getFileReader(scriptFile);
      
      // we're loading the script in memory completely, because undeploy deletes distribution files,
      // and we might delete the script file as it is being read from
      interpreter.interpret(new StringReader(IOUtil.textReaderToString(scriptReader)), StrLookups.merge(
          StrLookups.forKeyValues(
              "user.dir", scriptBaseDir.getAbsolutePath(),
              "corus.distribution.name", dist.getName(),
              "corus.distribution.version", dist.getVersion()
          ),
          PropertiesStrLookup.getInstance(ctx.getServerContext().getCorusProperties()),
          StrLookup.systemPropertiesLookup(),
          StrLookup.mapLookup(System.getenv())
      ));
    } catch (Throwable e) {
      throw new IllegalStateException("Could not execute rollback.corus script", e);
    } finally {
      try {
        scriptReader.close();
      } catch (IOException e) {
        // noop
      }
    }
  }

}
