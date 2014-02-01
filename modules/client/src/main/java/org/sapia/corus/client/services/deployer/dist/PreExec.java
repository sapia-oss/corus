package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of Corus command-line to be executed on the server-side.
 * 
 * @author yduchesne
 *
 */
public class PreExec implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private List<Cmd> commands = new ArrayList<Cmd>();
  
  /**
   * @param cmd a command-line to execute.
   */
  public void addCmd(Cmd cmd) {
    commands.add(cmd);
  }
  
  /**
   * @return the command-lines to execute.
   */
  public List<Cmd> getCommands() {
    return commands;
  }

}
