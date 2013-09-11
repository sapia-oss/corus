package org.sapia.corus.taskmanager.core;

import org.apache.log.Hierarchy;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.log.LoggerTaskLog;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;

public class TestTaskManager extends TaskManagerImpl{
  
  public TestTaskManager(ServerContext ctx) {
    super(new LoggerTaskLog(Hierarchy.getDefaultHierarchy().getLoggerFor("taskmanager")), 
          ctx, ThreadingConfiguration.newInstance());
  }

}
