package org.sapia.corus.deployer.handler;

import java.io.File;

import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata.Type;
import org.sapia.corus.client.services.deployer.transport.ShellScriptDeploymentMetadata;
import org.sapia.corus.deployer.DeploymentHandler;
import org.sapia.corus.deployer.InternalShellScriptManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link DeploymentHandler} for handling native shell scripts.
 * 
 * @author yduchesne
 * 
 */
public class ScriptDeploymentHandler implements DeploymentHandler {

  @Autowired
  private DeployerConfiguration configuration;

  @Autowired
  private InternalShellScriptManager manager;

  // --------------------------------------------------------------------------
  // Provided for testing

  public final void setConfiguration(DeployerConfiguration configuration) {
    this.configuration = configuration;
  }

  public final void setManager(InternalShellScriptManager manager) {
    this.manager = manager;
  }

  // --------------------------------------------------------------------------
  // DeploymentHandler interface

  @Override
  public boolean accepts(DeploymentMetadata meta) {
    return meta.getType() == Type.SCRIPT;
  }

  @Override
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File uploadedFile) {
    ShellScriptDeploymentMetadata scriptMeta = (ShellScriptDeploymentMetadata) meta;
    return manager.addScript(new ShellScript(scriptMeta.getAlias(), scriptMeta.getFileName(), scriptMeta.getDescription()), uploadedFile);
  }

  @Override
  public File getDestFile(DeploymentMetadata meta) {
    ShellScriptDeploymentMetadata fileMeta = (ShellScriptDeploymentMetadata) meta;
    return FilePath.newInstance().addDir(configuration.getScriptDir()).setRelativeFile(fileMeta.getFileName()).createFile();
  }

}
