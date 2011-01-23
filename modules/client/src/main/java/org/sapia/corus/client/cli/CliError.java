package org.sapia.corus.client.cli;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.command.CorusCliCommand;

/**
 * This class represents an error with the context of execution when it occured.
 * 
 * @author J-C Desrochers
 *
 */
public class CliError {

  private int _id;
  private long _timestamp;
  private String _description;
  private Throwable _cause;
  private CmdLine _commandLine;
  private CorusCliCommand _command;
  
  /**
   * Creates a new {@link CliError} instance.
   *
   * @param anId The identifier of the error.
   * @param aDescription An optionnal description of the error.
   * @param aCause The exception that caused this error.
   * @param aCommandLine The command line that was intepreted.
   * @param aCommand The executed command.
   */
  public CliError(int anId, String aDescription, Throwable aCause, CmdLine aCommandLine, CorusCliCommand aCommand) {
    _id = anId;
    _timestamp = System.currentTimeMillis();
    _description = aDescription;
    _cause = aCause;
    _commandLine = aCommandLine;
    _command = aCommand;
  }

  /**
   * Returns the id attribute.
   *
   * @return The id value.
   */
  public int getId() {
    return _id;
  }

  /**
   * Returns the timestamp attribute.
   *
   * @return The timestamp value.
   */
  public long getTimestamp() {
    return _timestamp;
  }

  /**
   * Returns the date of this error as a string.
   * 
   * @return The date of this error as a string.
   */
  public String getErrorDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
    return formatter.format(new Date(_timestamp));
  }

  /**
   * Returns the time of this error as a string.
   * 
   * @return The time of this error as a string.
   */
  public String getErrorTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    return formatter.format(new Date(_timestamp));
  }
  
  /**
   * Returns the description attribute.
   *
   * @return The description value.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Returns the cause attribute.
   *
   * @return The cause value.
   */
  public Throwable getCause() {
    return _cause;
  }

  /**
   * Returns the commandLine attribute.
   *
   * @return The commandLine value.
   */
  public CmdLine getCommandLine() {
    return _commandLine;
  }

  /**
   * Returns the command attribute.
   *
   * @return The command value.
   */
  public CorusCliCommand getCommand() {
    return _command;
  }

  public String getSimpleMessage() {
    StringBuilder buffer = new StringBuilder();
    
    if (_cause instanceof InputException) {
      buffer.append("Input error executing command '");
    } else if (_cause instanceof RuntimeException) {
      buffer.append("System error executing command '");
    } else {
      buffer.append("Error ").append(_cause.getClass().getSimpleName()).append(" executing command '");
    }
    
    buffer.append(_command.getName()).append("'");
    if (_description != null) {
      buffer.append(", ").append(_description);
    }
    buffer.append(" ==> ").append(_cause.getMessage());
    
    return buffer.toString();
  }
}
