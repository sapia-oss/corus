package org.sapia.corus.client.services.deployer;

/**
 * Thrown when a {@link ShellScript} is not found.
 * 
 * @author yduchesne
 * 
 */
public class ScriptNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;

  public ScriptNotFoundException(String msg) {
    super(msg);
  }

}
