package org.sapia.corus.admin;

import java.io.Serializable;

/**
 * Models a command argument.
 * @author yduchesne
 *
 */
public interface CommandArg extends Serializable{

  public boolean matches(String str);
 
}
