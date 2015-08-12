package org.sapia.corus.client.cli;

import java.util.List;
import java.util.Map;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.sort.SortSwitchInfo;

/**
 * This interface specifies the behavior common to command line contexts.
 * 
 * @author yduchesne
 * 
 */
public interface CliContext {

  /**
   * @return the {@link CorusConnector} used by the command line interface.
   */
  public CorusConnector getCorus();

  /**
   * @return the {@link ClientFileSystem}.
   */
  public ClientFileSystem getFileSystem();

  /**
   * @return the {@link CmdLine} instance corresponding to the command line that
   *         was entered.
   */
  public CmdLine getCommandLine();

  /**
   * @return the {@link Console} that is used to interact with the user.
   */
  public Console getConsole();

  /**
   * Returns the list of errors of this context.
   */
  public List<CliError> getErrors();

  /**
   * Creates a new {@link CliError} for the exception passed in and adds it to
   * this context.
   * 
   * @param aCommand
   *          The corus command executed.
   * @param aCause
   *          The exception that caused the error.
   * @return The created {@link CliError}.
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause);

  /**
   * Creates a new {@link CliError} for the exception passed in and adds it to
   * this context.
   * 
   * @param aCommand
   *          The corus command executed.
   * @param aDescription
   *          The description of th error.
   * @param aCause
   *          The exception that caused the error.
   * @return The created {@link CliError}.
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause);

  /**
   * Removes all errors from this context.
   * 
   * @return The number of errors that was removed.
   */
  public int removeAllErrors();

  /**
   * @return <code>true</code> if the command line interface should abort if an
   *         error occurs.
   */
  public boolean isAbordOnError();

  /**
   * @see #isAbordOnError()
   */
  public void setAbortOnError(boolean abortOnError);

  /**
   * @return the {@link StrLookupState} of variables that this instance holds.
   */
  public StrLookupState getVars();
  
  /**
   * @param vars a {@link Map} of variables to add.
   */
  public void addVars(Map<String, String> vars);
  
  /**
   * @return the sort switches that were passed.
   */
  public SortSwitchInfo[] getSortSwitches();

  /**
   * @param sortSwitches an array of sort switches.
   */
  public void setSortSwitches(SortSwitchInfo[] sortSwitches);
  
  /**
   * @param unternally unsets the sort switches.
   */
  public void unsetSortSwitches();

  /**
   * Sets the {@link AutoClusterFlag} info to use by commands supporting clustering (used by commands
   * such as <code>ripple</code>).
   * 
   * @param flag an {@link AutoClusterFlag}.
   */
  public void setAutoClusterInfo(AutoClusterFlag flag);
  
  /**
   * @return the {@link AutoClusterFlag} used for auto-clustering, if it has been set.
   */
  public OptionalValue<AutoClusterFlag> getAutoClusterInfo();
  
  /**
   * Reverses the effect of {@link #setAutoClusterInfo(AutoClusterFlag)}.
   */
  public void unsetAutoClusterInfo();
}