package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;

/**
 * Holds a server-side command-line to execute.
 * 
 * @author yduchesne
 *
 */
public class Cmd implements Serializable {

  private static final long serialVersionUID = 1L;

  private String value;
  
  /**
   * @param value a command-line to execute.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return a command line to execute.
   */
  public String getValue() {
    return value;
  }
  
  /**
   * @param cmdLine
   */
  public void setText(String cmdLine) {
    if (value != null) {
      value += cmdLine;
    } else {
      value = cmdLine;
    }
  }
  

}
