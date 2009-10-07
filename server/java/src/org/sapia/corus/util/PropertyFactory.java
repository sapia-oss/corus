package org.sapia.corus.util;

/**
 * This class implements a factory of {@link Property} instances.
 * @author yduchesne
 *
 */
public class PropertyFactory {
  
  public static Property create(long i){
    return new LongProperty(i);
  }
  
  public static Property create(int i){
    return new IntProperty(i);
  }
  
  public static Property create(String i){
    return new StringProperty(i);
  }
}
