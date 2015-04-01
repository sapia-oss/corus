package org.sapia.corus.cloud.topology;

/**
 * Interface implemented by classes whose instances can be "streamed" as XML content.
 * 
 * @author yduchesne
 *
 */
public interface XmlStreamable {

  /**
   * @param stream the {@link XmlStream} to output to.
   */
  public void output(XmlStream stream);
  
}
