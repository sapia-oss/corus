package org.sapia.corus.client.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;

/**
 * Allows defining the default {@link ContentLevel} for a resource that returns {@link ProgressResult} instances.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DefaultContentLevel {

  /**
   * The default {@link ContentLevel} to use when generating the JSON for a given {@link ProgressResult}s.
   */
  public ContentLevel value() default ContentLevel.DETAIL;

}
