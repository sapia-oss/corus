package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.CorusScript;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.ubik.util.Collects;

/**
 * Interprets a Corus script specified at the command-line.
 * 
 * @author yduchesne
 * 
 */
public class Script extends CorusCliCommand {

  private static final OptionDef ENGINE_OPT   = new OptionDef("e", true);
  private static final OptionDef INCLUDES_OPT = new OptionDef("i", true);
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(ENGINE_OPT, INCLUDES_OPT, OPT_CLUSTER);
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    Arg    fileName;
    String scriptEngineName = null;
    if (ctx.getCommandLine().containsOption(ENGINE_OPT.getName(), true)) {
      scriptEngineName = ctx.getCommandLine().assertOption(ENGINE_OPT.getName(), true).getValue();
    }
    fileName = getFirstArg(ctx);
    if (fileName == null) {
      throw new InputException("Path to script file expected");
    }

    try {
      File scriptFile = ctx.getFileSystem().getFile(fileName.getName());
      processScript(scriptFile, scriptEngineName, ctx);
    } catch (FileNotFoundException e) {
      throw new InputException(e.getMessage());
    } catch (Throwable e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to execute script", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  public void processScript(File scriptFile, String scriptEngineName, CliContext ctx) throws IOException, CommandNotFoundException, Throwable {
    if (scriptFile.exists()) {
      Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
      if (ctx.getAutoClusterInfo().isSet()) {
        interpreter.setAutoClusterInfo(ctx.getAutoClusterInfo().get());
      } else if (ctx.getCommandLine().containsOption(OPT_CLUSTER.getName(), false)) {
        interpreter.setAutoClusterInfo(AutoClusterFlag.forAll(getClusterInfo(ctx)));
      }
      if (scriptEngineName == null) {
        interpreter.interpret(new FileReader(scriptFile), ctx.getVars());
      } else {
        String[] includes = getOpt(ctx, INCLUDES_OPT.getName(), "").getValue().split(";");
        CorusScript scriptRunner = new CorusScript(interpreter, includes, scriptEngineName);
        scriptRunner.runScript(scriptFile.getAbsolutePath(), new HashMap<String, Object>());
      }
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }

}
