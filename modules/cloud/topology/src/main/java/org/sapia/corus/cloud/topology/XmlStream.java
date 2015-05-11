package org.sapia.corus.cloud.topology;

public interface XmlStream {
  
  public void beginRootElement(String name);
  
  public void beginElement(String name);
  
  public void attribute(String name, String value);

  public void attribute(String name, int value);
  
  public void cdata(String content);
  
  public void endElement(String name);
  
  public void endRootElement(String name);

}
