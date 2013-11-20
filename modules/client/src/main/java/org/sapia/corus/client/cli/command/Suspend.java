package org.sapia.corus.client.cli.command;

/**
 * Suspends currently running processes.
 * 
 * @author Yanick Duchesne
 */
public class Suspend extends Kill {
  public Suspend() {
    super(true);
  }
}
