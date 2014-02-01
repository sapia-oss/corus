package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;

/**
 * Holds a pre-executable CLI script meant to be executed on the server-side.
 * 
 * @author yduchesne
 *
 */
public class PreExec implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private String script;
  
  /**
   * Sets a script to execute (method is called setText to be compliant with how
   * the Config framework injects XML element content).
   * 
   * @param script a script's content.
   */
  public void setText(String script) {
    this.script = script;
  }
  
  /**
   * @return the script that was set.
   */
  public String getScript() {
    return script;
  }

}
