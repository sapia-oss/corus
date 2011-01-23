package org.sapia.corus.client.exceptions;

public class CorusException extends Exception{
  
  static final long serialVersionUID = 1L;
  
  private String code;
  
  public CorusException(String msg, String code) {
    super(msg);
    this.code = code;
  }

  public CorusException(String msg, String code, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }
  
  public String getCode(){
    return code;
  }
  

}
