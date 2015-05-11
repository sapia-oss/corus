package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Manages Corus dumps.
 * 
 * @author yduchesne
 *
 */
public class Dump extends CorusCliCommand {
    
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  public List<OptionDef> getAvailableOptions() {
    return new ArrayList<OptionDef>();
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    if (ctx.getCommandLine().isNextArg()) {
      File toSave = ctx.getFileSystem().getFile(ctx.getCommandLine().assertNextArg().getName());
      try (PrintWriter writer = new PrintWriter(toSave)) {
        String dump = ctx.getCorus().getContext().getCorus().dump();
        writer.print(dump);
        writer.flush();
      } catch (IOException e) {
        throw new AbortException("Could not open file: " + toSave.getAbsolutePath(), e);
      }
    } else {
      throw new InputException("Expected: dump <file>");
    }
    
  }
  
}
