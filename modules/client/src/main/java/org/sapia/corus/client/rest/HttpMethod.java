package org.sapia.corus.client.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows defining the HTTP method that is accepted by a resource.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface HttpMethod {
  
  public static final String GET    = "GET";
  public static final String POST   = "POST";
  public static final String PUT    = "PUT";
  public static final String DELETE = "DELETE";
  
  public String value();

}
