package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;

/**
 * Appends the process properties to the command line - these properties will be rendered 
 * according to the following format: <code>-Dname=value</code>.
 * 
 * @author yduchesne
 *
 */
public class IncludeProcessPropertiesArg implements CmdGenerator, Serializable {
  
  private static final long serialVersionUID = 1L;

  @Override
  public void generate(Env env, CmdLine toAppendTo) {
    for (Property p : env.getProperties()) {
      if (p.getValue() != null) {
        toAppendTo.addElement(p.convert());
      }
    }
  }
}
