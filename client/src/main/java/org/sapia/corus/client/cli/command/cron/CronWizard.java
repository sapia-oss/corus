package org.sapia.corus.client.cli.command.cron;

import java.io.IOException;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.Cron;
import org.sapia.corus.client.exceptions.cron.DuplicateScheduleException;
import org.sapia.corus.client.exceptions.cron.InvalidTimeException;
import org.sapia.corus.client.exceptions.processor.ProcessConfigurationNotFoundException;
import org.sapia.corus.client.services.cron.CronJobInfo;


/**
 * @author Yanick Duchesne
 */
public class CronWizard {
  public void execute(CmdLine cmd, CliContext ctx)
               throws AbortException, InputException {
    String      dist    = cmd.assertOption(Cron.DIST_OPT, true).getValue();
    String      version = cmd.assertOption(Cron.VERSION_OPT, true).getValue();
    String      profile = cmd.assertOption(Cron.PROFILE_OPT, true).getValue();
    String      vmName  = cmd.assertOption(Cron.VM_NAME_OPT, true).getValue();

    CronJobInfo info = new CronJobInfo(dist, profile, version, vmName);

    try {
      hour(info, ctx);
      minutes(info, ctx);
      dayOfWeek(info, ctx);
      dayOfMonth(info, ctx);
      month(info, ctx);
      year(info, ctx);
      ctx.getConsole().println("Cron job added; type 'cron list' for info.");
    } catch (AbortException e) {
      ctx.getConsole().println("Operation aborted.");
    } catch (IOException e) {
      ctx.getConsole().println("Error entering data.");
      e.printStackTrace(ctx.getConsole().out());
      throw new AbortException();
    }

    try {
      ctx.getCorus().getCronFacade().addCronJon(info);
    } catch (InvalidTimeException e) {
      throw new InputException(e.getMessage());
    } catch(DuplicateScheduleException e){
      throw new InputException(e.getMessage());
    } catch(ProcessConfigurationNotFoundException e){
      throw new InputException(e.getMessage());
    } catch (Exception e) {
      ctx.getConsole().println("Error entering data.");
      e.printStackTrace(ctx.getConsole().out());
      throw new AbortException();
    }
  }

  private void hour(CronJobInfo info, CliContext ctx)
             throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter HOUR of day at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every hour.");
    ctx.getConsole().println("Values must be between 0 and 24 inclusively.");
    ctx.getConsole().prompt();
    info.setHour(assertRange(process(ctx.getConsole().in().readLine()), 0, 24));
  }

  private void minutes(CronJobInfo info, CliContext ctx)
                throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter MINUTES in the hour at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every minute.");
    ctx.getConsole().println("Values must be between 0 and 59 inclusively.");
    ctx.getConsole().prompt();
    info.setMinute(assertRange(process(ctx.getConsole().in().readLine()), 0, 59));
  }

  private void dayOfWeek(CronJobInfo info, CliContext ctx)
                  throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter DAY OF WEEK at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every day.");
    ctx.getConsole().println("Values must be between 1 (SUNDAY) and 7 (SATURDAY) inclusively.");
    ctx.getConsole().prompt();
    info.setDayOfWeek(assertRange(process(ctx.getConsole().in().readLine()), 1,
                                  7));
  }

  private void dayOfMonth(CronJobInfo info, CliContext ctx)
                   throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter DAY OF MONTH at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every day.");
    ctx.getConsole().println("Values must be between 1 and 31 inclusively.");
    ctx.getConsole().prompt();
    info.setDayOfMonth(assertRange(process(ctx.getConsole().in().readLine()),
                                   1, 31));
  }

  private void month(CronJobInfo info, CliContext ctx)
              throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter MONTH of year at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every month.");
    ctx.getConsole().println("Values must be between 1 (JANUARY) and 12 (DECEMBER) inclusively.");
    ctx.getConsole().prompt();

    int value = process(ctx.getConsole().in().readLine());

    if (value < 0) {
      info.setMonth(value);
    } else {
      info.setMonth(assertRange(value, 1, 12) - 1);
    }
  }

  private void year(CronJobInfo info, CliContext ctx)
             throws IOException, AbortException, InputException {
    ctx.getConsole().println("Enter YEAR at which job should run,");
    ctx.getConsole().println("or enter '*' if it should run every year.");
    ctx.getConsole().prompt();
    info.setYear(process(ctx.getConsole().in().readLine()));
  }

  private static int assertRange(int value, int low, int high)
                          throws InputException {
    if (value < 0) {
      return value;
    }

    if ((value < low) || (value > high)) {
      throw new InputException("Value must be between " + low + " and " + high +
                               " inclusively");
    } else {
      return value;
    }
  }

  private int process(String input) throws AbortException, InputException {
    if ((input == null) || (input.length() == 0)) {
      throw new AbortException();
    }

    if (!input.equals(Cron.STAR)) {
      try {
        return Integer.parseInt(input);
      } catch (NumberFormatException e) {
        throw new InputException("invalid number: " + input);
      }
    } else {
      return CronJobInfo.UNDEFINED;
    }
  }
}
