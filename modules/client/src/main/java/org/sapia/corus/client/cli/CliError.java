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

  private int 						id;
  private long 						timestamp;
  private String 					description;
  private Throwable 			cause;
  private CmdLine 				commandLine;
  private CorusCliCommand command;
  
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
    id 						= anId;
    timestamp 		= System.currentTimeMillis();
    description 	= aDescription;
    cause 				= aCause;
    commandLine 	= aCommandLine;
    command 			= aCommand;
  }

  /**
   * Returns the id attribute.
   *
   * @return The id value.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the timestamp attribute.
   *
   * @return The timestamp value.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the date of this error as a string.
   * 
   * @return The date of this error as a string.
   */
  public String getErrorDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
    return formatter.format(new Date(timestamp));
  }

  /**
   * Returns the time of this error as a string.
   * 
   * @return The time of this error as a string.
   */
  public String getErrorTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    return formatter.format(new Date(timestamp));
  }
  
  /**
   * Returns the description attribute.
   *
   * @return The description value.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the cause attribute.
   *
   * @return The cause value.
   */
  public Throwable getCause() {
    return cause;
  }

  /**
   * Returns the commandLine attribute.
   *
   * @return The commandLine value.
   */
  public CmdLine getCommandLine() {
    return commandLine;
  }

  /**
   * Returns the command attribute.
   *
   * @return The command value.
   */
  public CorusCliCommand getCommand() {
    return command;
  }

  public String getSimpleMessage() {
    StringBuilder buffer = new StringBuilder();
    
    if (cause instanceof InputException) {
      buffer.append("Input error executing command '");
    } else if (cause instanceof RuntimeException) {
      buffer.append("System error executing command '");
    } else {
      buffer.append("Error ").append(cause.getClass().getSimpleName()).append(" executing command '");
    }
    
    buffer.append(command.getName()).append("'");
    if (description != null) {
      buffer.append(", ").append(description);
    }
    buffer.append(" ==> ").append(cause.getMessage());
    
    return buffer.toString();
  }
}
