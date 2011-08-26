package org.sapia.corus.client.exceptions;

public enum ExceptionCode {

  //// CORE

  INTERNAL_ERROR(ExceptionScope.CORE, 1000),
  SERVICE_NOT_FOUND(ExceptionScope.CORE, 1020),
  INVALID_INPUT(ExceptionScope.CORE, 1030),
  IO_ERROR(ExceptionScope.CORE, 1040),
  STALE_DATA(ExceptionScope.CORE, 1050),
 
  //// MISC

  MISSING_DATA(ExceptionScope.MISC, 1000),

  //// CLI

  COMMAND_HELP_ERROR(ExceptionScope.CLI, 1000),
  CONNECTION_ERROR(ExceptionScope.CLI, 1010),

  //// CRON

  INVALID_TIME(ExceptionScope.CRON_MODULE, 1000),
  DUPLICATE_SCHEDULE(ExceptionScope.CRON_MODULE, 1010),

  //// DEPLOYER
  
  DUPLICATE_DISTRIBUTION(ExceptionScope.DEPLOYER, 1000),
  DISTRIBUTION_NOT_FOUND(ExceptionScope.DEPLOYER, 1010),
  DEPLOYMENT_ERROR(ExceptionScope.DEPLOYER, 1020),
  CONCURRENT_DEPLOYMENT(ExceptionScope.DEPLOYER, 1030),
  RUNNING_PROCESSES(ExceptionScope.DEPLOYER, 1040),

  //// PORT MANAGER
  
  PORT_ACTIVE(ExceptionScope.PORT_MANAGER, 1000),
  PORT_RANGE_CONFLICT(ExceptionScope.PORT_MANAGER, 1010),
  PORT_RANGE_INVALID(ExceptionScope.PORT_MANAGER, 1020),
  PORT_UNAVAILABLE(ExceptionScope.PORT_MANAGER, 1030),

  //// PROCESSOR
  
  PROCESS_LOCK_UNAVAILABLE(ExceptionScope.PROCESSOR, 1000),
  PROCESS_CONFIG_NOT_FOUND(ExceptionScope.PROCESSOR, 1010),
  PROCESS_NOT_FOUND(ExceptionScope.PROCESSOR, 1020),
  TOO_MANY_PROCESS_INSTANCES(ExceptionScope.PROCESSOR, 1030),

  ;
  
  private ExceptionScope scope;
  private int code;
  private String fullCode;
  
  ExceptionCode(ExceptionScope scope, int code) {
    this.scope = scope;
    this.code = code;
    this.fullCode = scope.getCode() + "-" + code;
  }
  
  public ExceptionScope getScope() {
    return scope;
  }
  
  public String getFullCode(){
    return fullCode;
  }
  
  public int getCode() {
    return code;
  }

}
