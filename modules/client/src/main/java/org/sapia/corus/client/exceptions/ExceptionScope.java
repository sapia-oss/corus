package org.sapia.corus.client.exceptions;

/**
 * This enum holds the constants corresponding to the different error scopes.
 * 
 * @author yduchesne
 *
 */
public enum ExceptionScope {
  
  UNDEFINED(100),
  MISC(102),
  CLI(104),
  CORE(110),
  CLUSTER_MANAGER(200),
  CRON_MODULE(300),
  DATABASE_MODULE(400),
  DEPLOYER(500),
  HTTP_MODULE(600),
  PORT_MANAGER(700),
  PROCESSOR(800),
  TASK_MANAGER(900);

  private int code;
  
  private ExceptionScope(int code) {
    this.code = code;
  }
  
  public int getCode() {
    return code;
  }
}
