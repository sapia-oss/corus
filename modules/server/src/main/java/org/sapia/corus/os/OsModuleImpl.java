package org.sapia.corus.os;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.core.ModuleHelper;

@Bind(moduleInterface=OsModule.class)
public class OsModuleImpl extends ModuleHelper implements OsModule{
  
  /////////////// Lifecycle 
  
  @Override
  public void init() throws Exception {
  }
  
  @Override
  public void start() throws Exception {
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  /////////////// OsModule interface
  
  public String getRoleName() {
    return OsModule.ROLE;
  }
  
  @Override
  public String executeProcess(
      LogCallback log, 
      File rootDirectory,
      CmdLine commandLine) throws IOException {
    NativeProcess proc = NativeProcessFactory.newNativeProcess();
    return proc.exec(log, rootDirectory, commandLine);
  }
  
  @Override
  public void killProcess(LogCallback log, String pid) throws IOException {
    NativeProcess proc = NativeProcessFactory.newNativeProcess();
    proc.kill(log, pid);
  }

}
