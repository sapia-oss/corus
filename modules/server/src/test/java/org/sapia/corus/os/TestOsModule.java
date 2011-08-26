package org.sapia.corus.os;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.services.os.OsModule;

public class TestOsModule implements OsModule{

  private int pidCounter;
  
  @Override
  public synchronized String executeProcess(LogCallback log, File rootDirectory,
      CmdLine commandLine) throws IOException {
    return Integer.toString(++pidCounter);
  }
  
  @Override
  public void killProcess(LogCallback log, String pid) throws IOException {
  }

}