package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This component implements the {@link ShellScriptManager} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = { ShellScriptManager.class, InternalShellScriptManager.class })
@Remote(interfaces = { ShellScriptManager.class })
public class ShellScriptManagerImpl extends ModuleHelper implements InternalShellScriptManager {

  @Autowired
  private DbModule dbModule;

  @Autowired
  private FileSystemModule fileSystem;

  @Autowired
  private DeployerConfiguration deployerConfig;

  @Autowired
  private OsModule osModule;

  private ShellScriptDatabase database;

  // --------------------------------------------------------------------------
  // Provided for testing

  public final void setFileSystem(FileSystemModule fileSystem) {
    this.fileSystem = fileSystem;
  }

  public final void setDbModule(DbModule dbModule) {
    this.dbModule = dbModule;
  }

  public final void setDeployerConfig(DeployerConfiguration deployerConfig) {
    this.deployerConfig = deployerConfig;
  }

  public final void setDatabase(ShellScriptDatabase database) {
    this.database = database;
  }

  // --------------------------------------------------------------------------
  // Lifecycle

  @Override
  public void init() throws Exception {
    database = new ShellScriptDatabaseImpl(dbModule.getDbMap(String.class, ShellScript.class, "deployer.shellscripts"));
  }

  @Override
  public void dispose() throws Exception {
  }

  // --------------------------------------------------------------------------
  // Module interface

  @Override
  public String getRoleName() {
    return ShellScriptManager.ROLE;
  }

  // --------------------------------------------------------------------------
  // ShellScriptManager interface

  @Override
  public synchronized List<ShellScript> getScripts() {
    return database.getScripts();
  }

  @Override
  public synchronized List<ShellScript> getScripts(ShellScriptCriteria criteria) {
    return database.getScripts(criteria);
  }

  @Override
  public synchronized ProgressQueue removeScripts(ShellScriptCriteria criteria) {
    ProgressQueue progress = new ProgressQueueImpl();
    List<ShellScript> scripts = database.removeScript(criteria);

    // deleting existing script (there should be only one for the given alias);
    if (!scripts.isEmpty()) {
      for (ShellScript s : scripts) {
        progress.info("Removing existing shell script: " + s.getAlias());
        try {
          fileSystem.deleteFile(FilePath.newInstance().addDir(deployerConfig.getScriptDir()).setRelativeFile(s.getFileName()).createFile());
        } catch (FileNotFoundException e) {
          progress.warning("Current shell script file not found " + s.getFileName());
        } catch (Exception e) {
          progress.warning("Error deleting current shell script file " + s.getFileName());
        }
      }
    }
    progress.close();
    return progress;
  }

  @Override
  public ProgressQueue executeScript(String alias) throws ScriptNotFoundException, IOException {
    ShellScript script = this.database.getScript(alias);
    File scriptDir = new File(deployerConfig.getScriptDir());
    File scriptFile = new File(scriptDir, script.getFileName());
    if (!scriptFile.canExecute()) {
      throw new IOException("Script " + scriptFile.getName() + " is not executable");
    }
    final ProgressQueue progress = new ProgressQueueImpl();
    osModule.executeScript(new OsModule.LogCallback() {
      @Override
      public void error(String error) {
        progress.error(error);
        progress.close();
      }

      @Override
      public void debug(String msg) {
        progress.info(msg);
      }
    }, scriptDir, CmdLine.parse(scriptFile.getAbsolutePath()));
    progress.close();
    return progress;
  }
  
  @Override
  public synchronized void dump(JsonStream stream) {
    stream.field("shellScripts").beginObject();
    database.dump(stream);
    stream.endObject();
  }
  
  @Override
  public synchronized void load(JsonInput dump) {
    database.load(dump.getObject("shellScripts"));
  }

  // --------------------------------------------------------------------------
  // InternalShellScriptManager interface

  @Override
  public synchronized ProgressQueue addScript(ShellScript script, File file) {
    ProgressQueue progress = new ProgressQueueImpl();
    if (!database.getScripts(ShellScriptCriteria.newInstance().setAlias(ArgMatchers.exact(script.getAlias()))).isEmpty()) {
      progress.info("Replacing existing script for alias: " + script.getAlias());
    }
    database.addScript(script);
    file.setExecutable(true, true);
    progress.close();
    return progress;
  }

  @Override
  public File getScriptFile(ShellScript script) throws FileNotFoundException {
    File toReturn = new File(new File(deployerConfig.getScriptDir()), script.getFileName());
    if (!fileSystem.exists(toReturn)) {
      throw new FileNotFoundException("Shell script file not found: " + toReturn.getAbsolutePath());
    }
    return toReturn;
  }

}
