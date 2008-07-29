package org.sapia.corus.admin;

public class StringCommandArg implements CommandArg {
  
  public static final long serialVersionUID = 1L;
  
  private String _token;
  
  public StringCommandArg(String token) {
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
