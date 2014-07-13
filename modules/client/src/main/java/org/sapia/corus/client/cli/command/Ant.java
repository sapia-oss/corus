package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.PropertyHelper;
import org.sapia.console.AbortException;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.cli.CliContext;

/**
 * Executes Ant scripts.
 * 
 * @author yduchesne
 *
 */
public class Ant extends CorusCliCommand {
  
  private static final String OPT_FILE   = "f";
  private static final String OPT_LEVEL  = "l";
  private static final String OPT_TARGET = "t";
  
  private static final Map<String, Integer> LOG_LEVELS_BY_NAME = new HashMap<String, Integer>();
  static {
    LOG_LEVELS_BY_NAME.put("verbose", Project.MSG_VERBOSE);
    LOG_LEVELS_BY_NAME.put("debug", Project.MSG_DEBUG);
    LOG_LEVELS_BY_NAME.put("info", Project.MSG_INFO);
    LOG_LEVELS_BY_NAME.put("warn", Project.MSG_WARN);
    LOG_LEVELS_BY_NAME.put("error", Project.MSG_ERR);
  }
  
  @Override
  protected void doExecute(final CliContext ctx) throws AbortException, InputException {
   
    File f = ctx.getFileSystem().getFile(ctx.getCommandLine().getOptNotNull(OPT_FILE).getValueNotNull());
    
    int logLevel = Project.MSG_INFO;
    if (ctx.getCommandLine().containsOption(OPT_LEVEL, false)) {
      String levelName = ctx.getCommandLine().getOptNotNull(OPT_LEVEL).getValueNotNull();
      Integer specifiedLevel = LOG_LEVELS_BY_NAME.get(levelName);
      if (specifiedLevel == null) {
        throw new InputException(String.format("Invalid log level: %s. Should be one of: verbose, debug, info, warn, error", specifiedLevel));
      }
      logLevel = specifiedLevel;
    } else if (ctx.getCommandLine().containsOption("d", false) || ctx.getCommandLine().containsOption("debug", false)) {
      logLevel = Project.MSG_DEBUG;
    } else if (ctx.getCommandLine().containsOption("v", false) || ctx.getCommandLine().containsOption("verbose", false)) {
      logLevel = Project.MSG_VERBOSE;
    }
    
    ctx.getConsole().println("Executing Ant script: " + f.getAbsolutePath());
    
    try {
      BuildLogger logger = new ConsoleBuildLogger(ctx.getConsole().out());
      logger.setMessageOutputLevel(logLevel);
      
      Option targetOpt = ctx.getCommandLine().getOpt(OPT_TARGET);
      
      Project p = new Project();
      p.setBaseDir(ctx.getFileSystem().getBaseDir());
      p.setUserProperty("ant.file", f.getAbsolutePath());
      p.init();

      PropertyHelper.getPropertyHelper(p).add(new PropertyHelper.PropertyEvaluator() {
        @Override
        public Object evaluate(String name, PropertyHelper helper) {
          return ctx.getVars().lookup(name);
        }
      });

      p.addBuildListener(logger);
      ProjectHelper helper = ProjectHelper.getProjectHelper();
      p.addReference("ant.projectHelper", helper);
            
      helper.parse(p, f);
      p.executeTarget(targetOpt != null ? targetOpt.getValueNotNull() : p.getDefaultTarget());
      ctx.getConsole().println("Completed Ant script execution");
      
    } catch (BuildException e) {
      throw new AbortException("Error executing build script", e);
    }
  }
  
  // ==========================================================================
  
  static class ConsoleBuildLogger implements BuildLogger {
    
    private ConsoleOutput out;
    private int messageOutputLevel;
    
    public ConsoleBuildLogger(ConsoleOutput out) {
      this.out = out;
    }
    
    @Override
    public void buildFinished(BuildEvent paramBuildEvent) {
    }
    @Override
    public void buildStarted(BuildEvent paramBuildEvent) {
    }
   
    @Override
    public void targetStarted(BuildEvent paramBuildEvent) {
    }
    
    @Override
    public void targetFinished(BuildEvent paramBuildEvent) {
    }

    @Override
    public void taskStarted(BuildEvent paramBuildEvent) {
    }
    
    @Override
    public void taskFinished(BuildEvent paramBuildEvent) {
    }
   
    @Override
    public void messageLogged(BuildEvent paramBuildEvent) {
      if (paramBuildEvent.getPriority() <= messageOutputLevel
          && paramBuildEvent.getTask() != null && paramBuildEvent.getMessage() != null) {
        out.println(String.format("[%s] %s", paramBuildEvent.getTask().getTaskName(), paramBuildEvent.getMessage()));
      }
    }
    
    @Override
    public void setErrorPrintStream(PrintStream paramPrintStream) {
    }

    @Override
    public void setOutputPrintStream(PrintStream paramPrintStream) {
    }
    
    @Override
    public void setMessageOutputLevel(int level) {
      messageOutputLevel = level;
    }
    
    @Override
    public void setEmacsMode(boolean paramBoolean) {
    }
  }
  
}
