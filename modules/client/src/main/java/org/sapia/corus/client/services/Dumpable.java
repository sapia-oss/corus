package org.sapia.corus.client.services;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;

/**
 * Specifies dump/load behavior.
 * 
 * @author yduchesne
 *
 */
public interface Dumpable {

  /**
   * Dumps this instance's content to the given stream.
   * 
   * @param the {@link JsonStream} to dump to.
   */
  public void dump(JsonStream stream);
  
  /**
   * @param dump a dump in JSON format.
   */
  public void load(JsonInput dump);
}
