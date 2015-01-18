package org.sapia.corus.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sapia.corus.client.services.security.Permission;

/**
 * Annotates methods with specific permissions.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Authorized {

  public Permission[] value() default { Permission.READ };
}
