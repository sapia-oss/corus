package org.sapia.corus.client.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows defining the accepted content types of a resources.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Accepts {
  
  public String[] value();

}
