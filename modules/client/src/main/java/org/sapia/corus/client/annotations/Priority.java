package org.sapia.corus.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows tagging a method or a class with a {@link PriorityLevel}. This can be convenient 
 * in the context of chains of responsibilities.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Priority {
  
  /**
   * This instance's {@link PriorityLevel}.
   */
  PriorityLevel value() default PriorityLevel.MINIMAL;

}
