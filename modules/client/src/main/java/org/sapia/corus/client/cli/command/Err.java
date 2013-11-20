package org.sapia.corus.client.cli.command;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.TableDef;

/**
 * This class contains the implementation of the 'err' command used to display
 * error logs on the corus CLI.
 * 
 * @author J-C Desrochers
 */
public class Err extends CorusCliCommand {

  private TableDef ERR_DETAIL = TableDef.newInstance().createCol("name", 10).createCol("value", 66);
  private TableDef ERR_LIST = TableDef.newInstance().createCol("id", 4).createCol("msg", 72);

  // --------------------------------------------------------------------------

  private static final String OPTION_ERROR_LIST = "l";
  private static final String OPTION_ERROR_ID = "i";
  private static final String OPTION_ERROR_CLEAR = "c";

  // --------------------------------------------------------------------------

  public void doExecute(CliContext aContext) throws AbortException, InputException {

    // 1. Command has no option/argument
    if (!aContext.getCommandLine().hasNext()) {
      doShowDetailsLastError(aContext);

      // 2. Command starts with an option
    } else if (aContext.getCommandLine().isNextOption()) {
      Option o = (Option) aContext.getCommandLine().next();
      if (OPTION_ERROR_LIST.equals(o.getName())) {
        try {
          if (o.getValue() == null) {
            doShowErrorList(aContext, Integer.MAX_VALUE);
          } else {
            doShowErrorList(aContext, Integer.parseInt(o.getValue()));
          }
        } catch (NumberFormatException nfe) {
          throw new InputException("Option " + o + " is invalid: it must have a numeric value");
        }

      } else if (OPTION_ERROR_ID.equals(o.getName())) {
        try {
          doShowErrorDetails(aContext, Integer.parseInt(o.getValue()));
        } catch (NumberFormatException nfe) {
          throw new InputException("Option " + o + " is invalid: it must have a numeric value");
        }

      } else if (OPTION_ERROR_CLEAR.equals(o.getName())) {
        doClearErrors(aContext);

      } else {
        throw new InputException("Option " + o + " is not supported");
      }

      // 3. Command starts with an argument
    } else {
      Arg a = (Arg) aContext.getCommandLine().next();
      throw new InputException("Argument " + a + " is not supported");
    }
  }

  /**
   * Showing the details of the last error on the console.
   * 
   * @param aContext
   *          The CLI context.
   */
  private void doShowDetailsLastError(CliContext aContext) {
    List<CliError> errors = aContext.getErrors();
    if (errors.isEmpty()) {
      aContext.getConsole().println("Currently no error in memory");
    } else {
      displayErrorDetailsInTable(aContext, errors.get(0));
    }
  }

  /**
   * Showing the list of the last errors.
   * 
   * @param aContext
   *          The CLI context.
   * @param aMaxCount
   *          The maximum number of errors to show.
   */
  private void doShowErrorList(CliContext aContext, int aMaxCount) {
    Table table = ERR_LIST.createTable(aContext.getConsole().out());

    table.drawLine('=', 0, CONSOLE_WIDTH);
    aContext.getConsole().println(" ERROR LIST");
    table.drawLine('=', 0, CONSOLE_WIDTH);

    Row headers = table.newRow();
    headers.getCellAt(ERR_LIST.col("id").index()).append("Id");
    headers.getCellAt(ERR_LIST.col("msg").index()).append("Error");
    headers.flush();

    table.drawLine('-', 0, CONSOLE_WIDTH);

    int count = 0;
    for (Iterator<CliError> it = aContext.getErrors().iterator(); it.hasNext() && count++ < aMaxCount;) {
      CliError error = it.next();

      Row data = table.newRow();
      data.getCellAt(ERR_LIST.col("id").index()).append(String.valueOf(error.getId()));
      data.getCellAt(ERR_LIST.col("msg").index()).append(error.getSimpleMessage());
      data.flush();
    }

    table.drawLine('=', 0, CONSOLE_WIDTH);
    aContext.getConsole().println();
  }

  /**
   * Shows the details of an error on the console.
   * 
   * @param aContext
   *          The CLI context.
   * @param anErrorId
   *          The identifier of the error to show.
   * @exception InputException
   *              If no error is found for the identifier passed in.
   */
  private void doShowErrorDetails(CliContext aContext, int anErrorId) throws InputException {
    CliError foundError = null;
    for (Iterator<CliError> it = aContext.getErrors().iterator(); it.hasNext() && foundError == null;) {
      CliError e = it.next();
      if (e != null && anErrorId == e.getId()) {
        foundError = e;
      }
    }

    if (foundError == null) {
      aContext.getConsole().println("No error found for id " + anErrorId);
    } else {
      displayErrorDetailsInTable(aContext, foundError);
    }
  }

  /**
   * Clears the errors from memory.
   * 
   * @param aContext
   */
  private void doClearErrors(CliContext aContext) {
    int count = aContext.removeAllErrors();
    if (count == 0) {
      aContext.getConsole().println("No error to clear from memory");

    } else if (count == 1) {
      aContext.getConsole().println("Cleared a single from memory");

    } else {
      aContext.getConsole().println("Cleared " + count + " errors from memory");
    }
  }

  private void displayErrorDetailsInTable(CliContext aContext, CliError anError) {
    Table table = ERR_DETAIL.createTable(aContext.getConsole().out());

    table.drawLine('=', 0, CONSOLE_WIDTH);
    aContext.getConsole().println(" ERROR DETAILS");
    table.drawLine('=', 0, CONSOLE_WIDTH);

    // Error id
    Row data = table.newRow();
    data.getCellAt(ERR_DETAIL.col("name").index()).append("ID       :");
    data.getCellAt(ERR_DETAIL.col("value").index()).append(String.valueOf(anError.getId()));
    data.flush();

    // Error date and time
    data = table.newRow();
    data.getCellAt(ERR_DETAIL.col("name").index()).append("DATE     :");
    data.getCellAt(ERR_DETAIL.col("value").index()).append(anError.getErrorDate() + " " + anError.getErrorTime());
    data.flush();

    // Command line
    data = table.newRow();
    data.getCellAt(ERR_DETAIL.col("name").index()).append("COMMAND  :");
    data.getCellAt(ERR_DETAIL.col("value").index()).append(anError.getCommand().getName() + " " + anError.getCommandLine().toString());
    data.flush();

    // Error message
    data = table.newRow();
    data.getCellAt(ERR_DETAIL.col("name").index()).append("MESSAGE  :");
    data.getCellAt(ERR_DETAIL.col("value").index()).append(anError.getSimpleMessage());
    data.flush();

    if (anError.getCause() != null) {
      // Exception
      table.drawLine('-', 0, CONSOLE_WIDTH);
      aContext.getConsole().print(" CAUSED BY:  ");
      aContext.getConsole().out().flush();
      aContext.getConsole().out().println(ExceptionUtils.getStackTrace(anError.getCause()));
    }

    table.drawLine('=', 0, CONSOLE_WIDTH);
    aContext.getConsole().println();
  }

}
