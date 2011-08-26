package org.sapia.corus.taskmanager;

import org.apache.tools.ant.Project;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * Wraps an Ant task in a {@link Task} instance.
 * 
 * @author Yanick Duchesne
 */
public class AntTaskHelper {
  
  public static Task<Void,Void> init(String name, org.apache.tools.ant.Task antTask) {
    return new DynAntTask(name, antTask);
  }

  static class DynAntTask extends Task<Void,Void>{
    private org.apache.tools.ant.Task _antTask;

    DynAntTask(String name, org.apache.tools.ant.Task antTask) {
      super(name);
      _antTask = antTask;
    }
    
    @Override
    public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
      try {
        Project p = new Project();
        p.addBuildListener(new AntTaskLog(ctx));
        _antTask.setProject(p);
        _antTask.execute();
      } catch (Throwable t) {
        ctx.error(t);
      }
      return null;
    }
  }
}
