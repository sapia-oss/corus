package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>cluster</code> element.
 * 
 * @author yduchesne
 *
 */
public class Cluster extends ClusterTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  @Override
  public void render(TopologyContext context) {
    if (templateRef != null) {
      ClusterTemplate template = context.resolveClusterTemplate(templateRef);
      copyFrom(template);
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (getName() == null) {
      throw new IllegalArgumentException("'name' attribute not set on <cluster>");
    }
    if (getInstances() <= 0) {
      throw new IllegalArgumentException("Invalid value for 'instances' attribute of <cluster> element: " + getName() + ". Must be > 0");
    }
    if (getMachines().isEmpty()) {
      throw new IllegalArgumentException("<cluster> element " + getName() + " does not have any <machine> child element");
    }
    
    for (Machine m  : getMachines()) {
      m.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreamable

  @Override
  public void output(XmlStream stream) {
    stream.beginElement("cluster");
    stream.attribute("name", getName());
    stream.attribute("instances", getInstances());
    for (Machine m : getMachines()) {
      m.output(stream);
    }
    stream.endElement("cluster");
  }
}
