package org.sapia.corus.cloud.topology;

/**
 * Models the association between a {@link Machine} and a load balancer.
 * 
 * @author yduchesne
 *
 */
public class LoadBalancerAttachment implements XmlStreamable, Validateable {

  private String name;
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("'name' attribute not set on <load-balancer-attachment> element");
    }
  }
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("load-balancer-attachment");
    stream.attribute("name", name);
    stream.endElement("load-balancer-attachment");
  }  
  
  @Override
  public int hashCode() {
    return name == null ? super.hashCode() : name.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LoadBalancerAttachment) {
      LoadBalancerAttachment other = (LoadBalancerAttachment) obj;
      return name.equals(other.name);
    }
    return false;
  }
}
