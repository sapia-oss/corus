package org.sapia.corus.cloud.topology;

/**
 * Models an artifact to be deployed.
 * 
 * @author yduchesne
 *
 */
public class Artifact implements Validateable, XmlStreamable {

  private String url;
  
  public Artifact() {
  }
  
  public Artifact(String url) {
    this.url = url;
  }
  
  /**
   * @return the URL string of the artifact to deploy.
   */
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (url == null) {
      throw new IllegalArgumentException("'name' attribute not set on <artifact> element");
    }
  }
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("artifact");
    stream.attribute("url", url);
    stream.endElement("artifact");
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Artifact) {
      Artifact other = (Artifact) obj;
      if (url == null || other.getUrl() == null) {
        return false;
      } else {
        return url.equals(other.getUrl());
      }
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return url == null ? super.hashCode() : url.hashCode();
  }

}
