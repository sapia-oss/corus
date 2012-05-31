package org.sapia.corus.client.cli;

import org.sapia.console.ReflectCommandFactory;

public class CorusCommandFactory extends ReflectCommandFactory{

  public CorusCommandFactory() {
    addPackage("org.sapia.corus.client.cli.command");
  }
}
