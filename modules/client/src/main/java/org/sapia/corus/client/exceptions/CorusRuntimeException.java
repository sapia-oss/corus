package org.sapia.corus.client.exceptions;

public class CorusRuntimeException extends RuntimeException {

  static final long serialVersionUID = 1L;

  private String code;

  public CorusRuntimeException(String msg, String code) {
    super(msg);
    this.code = code;
  }

  public CorusRuntimeException(String msg, String code, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  public String getCode() {
    return code;
  }

}
