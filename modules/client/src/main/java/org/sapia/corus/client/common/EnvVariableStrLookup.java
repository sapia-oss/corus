package org.sapia.corus.client.common;

import org.apache.commons.lang.text.StrLookup;

/**
 * Returns values for environment variables, given keys that are meant
 * to correspond to environment variable names.
 * 
 * @author yduchesne
 *
 */
public class EnvVariableStrLookup extends StrLookup {
  
  @Override
  public String lookup(String key) {
    return System.getenv(key);
  }

}
