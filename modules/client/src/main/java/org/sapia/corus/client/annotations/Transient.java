package org.sapia.corus.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
/**
 * Used to tag fields (or in some case methods) to indicate that the corresponding
 * property should not be persisted.
 * 
 * @author yduchesne
 *
 */
public @interface Transient {

}
