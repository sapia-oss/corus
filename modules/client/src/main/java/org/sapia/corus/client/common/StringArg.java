package org.sapia.corus.client.common;

/**
 * An instance of this class is used to match given strings exactly.
 * 
 * @author yduchesne
 * 
 */
public class StringArg implements Arg {

  public static final long serialVersionUID = 1L;

  private String token;

  public StringArg(String token) {
    this.token = token;
  }

  public boolean matches(String str) {
    if (str == null)
      return false;
    else
      return token.equals(str);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof StringArg) {
      return token.equals(((StringArg) obj).token);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return token.hashCode();
  }

  public String toString() {
    return token;
  }

}
