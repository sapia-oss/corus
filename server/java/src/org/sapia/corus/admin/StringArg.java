package org.sapia.corus.admin;

/**
 * An instance of this class is used to match given strings
 * exactly.
 * 
 * @author yduchesne
 *
 */
public class StringArg implements Arg {
  
  public static final long serialVersionUID = 1L;
  
  private String _token;
  
  public StringArg(String token) {
    _token = token;
  }
  
  public boolean matches(String str) {
    if(str == null) return false;
    else return _token.equals(str);
  }
  
  public String toString(){
    return _token;
  }

}
