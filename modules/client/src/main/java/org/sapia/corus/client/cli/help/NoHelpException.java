package org.sapia.corus.client.cli.help;

/**
 * @author Yanick Duchesne
 */
public class NoHelpException extends Exception{
  
  static final long serialVersionUID = 1L;

  public NoHelpException(){
    super("No help available");
  }
}
