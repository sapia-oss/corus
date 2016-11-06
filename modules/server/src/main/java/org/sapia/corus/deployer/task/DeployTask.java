package org.sapia.corus.deployer.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.cli.EmbeddedInterpreter;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.StrLookups;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Distribution.State;
import org.sapia.corus.client.services.deployer.event.DeploymentScriptExecutedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStartingEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentUnzippedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Status;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Type;
import org.sapia.corus.client.services.deployer.event.RollbackStartingEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.deployer.DeployerThrottleKeys;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentProcessorManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * This task handles the extraction of deployment jars from the temporary file
 * (where they have been uploaded) to the deployment directory. This task
 * ensures that a distribution will not overwrite an existing one, and cleans up
 * the temporary jar. Distributions are stored under the deployment directory,
 * according to the following pattern:
 * <p>
 * <code>distribution_name/version</code>
 * <p>
 * Each distribution directory has two additional directories:
 * <code>common</code> (where the jar is actually extracted) and, eventually,
 * <code>processes</code>, where each process instance has a specific directory
 * that it owns exclusively.
 * <p>
 * Also, this task will attempting will check the {@link DeployPreferences#isExecuteDeployScripts()} flag 
 * and, if set to <code>true</code>, will try to execute deployment scripts packaged under 
 * <tt>META-INF</tt>/<tt>scripts</tt>. These scripts are expected to be the following:
 * <ul>
 *    <li><tt>pre-deploy.corus</tt>: executed before the distribution is actually added to Corus
 *    (that script is mandatory).
 *    <li><tt>post-deploy.corus</tt>: executed after the distribution is actually added to Corus
 *    (that script is optional).
 *    <li><tt>rollback.corus</tt>: executed if an error occurs upon deployment
 *    (that script is optional).
 * </ul>
 * 
 * @author Yanick Duchesne
 */
public class DeployTask extends Task<Void, TaskParams<File, DeployPreferences, Void, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return DeployerThrottleKeys.DEPLOY_DISTRIBUTION;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<File, DeployPreferences, Void, Void> params) throws Throwable {
    doExecute(ctx, params);
    return null;
  }
  
  private void doExecute(TaskExecutionContext ctx, TaskParams<File, DeployPreferences, Void, Void> params) throws Throwable {

    File              srcZip        = params.getParam1();
    String            distFileName  = srcZip.getName();
    DeployPreferences prefs         = params.getParam2();
    
    DistributionDatabase dists            = ctx.getServerContext().lookup(DistributionDatabase.class);
    Deployer             deployer         = ctx.getServerContext().getServices().getDeployer();
    FileSystemModule     fs               = ctx.getServerContext().getServices().getFileSystem();
    EventDispatcher      dispatcher       = ctx.getServerContext().getServices().getEventDispatcher();
    DeploymentProcessorManager processor  = ctx.getServerContext().getServices().getDeploymentProcessorManager();
    RepositoryConfiguration repoConf      = ctx.getServerContext().getServices().lookup(RepositoryConfiguration.class);

    
    String tmpBaseDirName = null;
    String baseDirName    = null;
    File   tmpBaseDir     = null;
    File   baseDir        = null;
    File   commonDir      = null;
  
    dispatcher.dispatch(new DeploymentStartingEvent());
    
    // extracting corus.xml from archive and checking if already exists...
    Distribution dist = null; 
    try {
      dist = Distribution.newInstance(srcZip, fs);
    } catch (DeploymentException e) {
      dispatcher.dispatch(new DeploymentFailedEvent());
      fs.deleteFile(srcZip);
      throw e;
    }

    try {

      ctx.info(String.format("Unpackaging: %s", distFileName));
      if (shouldRunDeployScripts(ctx,  prefs, repoConf)) {
        ctx.info("Will run packaged deployment scripts");
      }
      baseDirName = FilePath.newInstance().addDir(deployer.getConfiguration().getDeployDir())
          .addDir(dist.getName())
          .addDir(dist.getVersion())
          .createFilePath();
      
      tmpBaseDirName = FilePath.newInstance().addDir(deployer.getConfiguration().getDeployDir())
          .addDir(dist.getName())
          .addDir(dist.getVersion() + "-temp")
          .createFilePath();
      
      dist.setBaseDir(baseDirName);
      
      tmpBaseDir = fs.getFileHandle(tmpBaseDirName);
      baseDir    = fs.getFileHandle(baseDirName);

      commonDir       = FilePath.newInstance().addDir(tmpBaseDirName).addDir("common").createFile();
      File processDir = FilePath.newInstance().addDir(tmpBaseDirName).addDir("processes").createFile();

      DistributionCriteria criteria = DistributionCriteria.builder().name(dist.getName()).version(dist.getVersion()).build();
      if (dists.containsDistribution(criteria) && !prefs.isExecuteDeployScripts()) {
        ctx.error(new DuplicateDistributionException("Distribution already exists for: " + dist.getName() + " version: " + dist.getVersion()));
        dispatcher.dispatch(new DeploymentFailedEvent(dist));
        return;
      }

      // making distribution directories...
      try {
        fs.createDirectory(commonDir);
      } catch (IOException e) {
        ctx.error(String.format("Could not make directory: %s", commonDir.getAbsolutePath()));
        dispatcher.dispatch(new DeploymentFailedEvent(dist));
      }
      try {
        fs.createDirectory(processDir);
      } catch (IOException e) {
        ctx.error(String.format("Could not make directory: %s", processDir.getAbsolutePath()));
        dispatcher.dispatch(new DeploymentFailedEvent(dist));
      }
      
      fs.unzip(srcZip, commonDir);

      if (shouldRunDeployScripts(ctx,  prefs, repoConf)) {
        doRunDeployScript(ctx, dispatcher, fs, dist, commonDir, "pre-deploy.corus", true);
      }
      
      dist.setTimestamp(baseDir.lastModified());
      dists.addDistribution(dist);
      fs.renameDirectory(tmpBaseDir, baseDir);
      commonDir = FilePath.newInstance().addDir(baseDirName).addDir("common").createFile();
      processDir = FilePath.newInstance().addDir(baseDirName).addDir("processes").createFile();
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new DeploymentUnzippedEvent(dist));
      
      if (shouldRunDeployScripts(ctx,  prefs, repoConf)) {
        doRunDeployScript(ctx, dispatcher, fs, dist, commonDir, "post-deploy.corus", false);
      }
      dist.setState(State.DEPLOYED);
      processor.onPostDeploy(new DeploymentContext(dist), new TaskLogCallback(ctx));
      ctx.info("Distribution added to Corus");
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new DeploymentCompletedEvent(dist));
      
    } catch (Exception e) {
      ctx.warn("Deloyment resulted in an error", e);
      dispatcher.dispatch(new DeploymentFailedEvent(dist));
      if (tmpBaseDir != null) {
        try {
          processor.onPostUndeploy(new DeploymentContext(dist), new TaskLogCallback(ctx));
        } catch (Exception pe) {
          ctx.warn("Got non-critical error while undeploying (moving ahead with remaining undeployment steps)", pe);
        }
        if (shouldRunDeployScripts(ctx,  prefs, repoConf)) {
          ctx.info("Error occured while deploying. Will execute rollback.corus script if it is provided in the distribution");
          try {
            // tmpBaseDir might or might not have been renamed to point to baseDir at this point
            File   actualBaseDir = tmpBaseDir.exists() ? tmpBaseDir : baseDir;
            String scriptBaseDir = FilePath.newInstance().addDir(actualBaseDir.getAbsolutePath()).addDir("common").createFilePath();
            dispatcher.dispatch(new RollbackStartingEvent(dist));
            if(doRunDeployScript(ctx, dispatcher, fs, dist, fs.getFileHandle(scriptBaseDir), "rollback.corus", false)) {
              ctx.getServerContext().getServices().getEventDispatcher().dispatch(new RollbackCompletedEvent(dist, Type.AUTO, Status.SUCCESS));
            }
            ctx.error("Rollback completed. Was automatically performed due to error:", e);
          } catch (Exception e2) {
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new RollbackCompletedEvent(dist, Type.AUTO, Status.FAILURE));
            ctx.error("Error executing rollback.corus script", e2);
            ctx.error("Original error that caused rollback was:", e);
          }
        } else {
          dispatcher.dispatch(new DeploymentFailedEvent(dist));
          ctx.error("Deployment error occurred", e);
        }
        if (!(e instanceof DuplicateDistributionException) && baseDir != null && baseDir.exists()) {
          dists.removeDistribution(DistributionCriteria.builder().name(dist.getName()).version(dist.getVersion()).build());
          fs.deleteDirectory(baseDir);
        }
      } else {
        dispatcher.dispatch(new DeploymentFailedEvent(dist));
        ctx.error("Deployment error occurred", e);
      }
    } finally {
      fs.deleteFile(srcZip);
      if (tmpBaseDirName != null) {
        // using handle at this point, since tmpBaseDir (File instance) has been renamed to point to baseDir.
        fs.deleteDirectory(fs.getFileHandle(tmpBaseDirName));
      }
    }
  }
  
  private boolean doRunDeployScript(
      final TaskExecutionContext ctx, 
      EventDispatcher eventDispatcher, 
      FileSystemModule fs, 
      Distribution dist, 
      File scriptBaseDir, 
      String scriptName, 
      boolean mandatory) 
      throws FileNotFoundException, IllegalStateException {
    
    FilePath scriptDirPath = FilePath.newInstance()
        .addDir(scriptBaseDir.getAbsolutePath())
        .addDir("META-INF")
        .addDir("scripts"); 
    File deployScript = fs.getFileHandle(scriptDirPath.setRelativeFile(scriptName).createFilePath());
    if (!deployScript.exists()) {
      if (mandatory) {
        throw new FileNotFoundException(scriptName + " not found under META-INF/scripts");
      } else {
        ctx.info(deployScript.getAbsolutePath() + " not found, will not be executed");
        return false;
      }
    }
    
    ctx.info("Executing script: " + scriptName);
    
    EmbeddedInterpreter interpreter  = new EmbeddedInterpreter(ctx.getServerContext().getCorus(), scriptBaseDir);
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
    Reader scriptReader = null;
    try {
      scriptReader = fs.getFileReader(deployScript);
      // we're loading the script in memory completely, because if the script does an undeploy
      // (which makes sense in the context of a rollback), it deletes distribution files, and 
      // we might delete the script file as deletion is occurring.
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
      
      eventDispatcher.dispatch(new DeploymentScriptExecutedEvent(scriptName));
      
      return true;
    } catch (Throwable e) {
      throw new IllegalStateException("Could not execute " + scriptName + " script", e);
    } finally {
      try {
        if (scriptReader != null) {
          scriptReader.close();
        }
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  private boolean shouldRunDeployScripts(TaskExecutionContext context, DeployPreferences prefs, RepositoryConfiguration conf) {
    if (context.getServerContext().getCorusHost().getRepoRole() == RepoRole.CLIENT) {
      return  conf.isRepoClientDeployScriptEnabled();
    } else {
      return prefs.isExecuteDeployScripts();
    }
  }
}
