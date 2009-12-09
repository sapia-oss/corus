package org.sapia.corus.admin.cli;

import org.sapia.console.ReflectCommandFactory;

public class CorusCommandFactory extends ReflectCommandFactory{
  
  public CorusCommandFactory() {
    addPackage("org.sapia.corus.admin.cli.command");
  }
 

}
