package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>env</code> element.
 * 
 * @author yduchesne
 *
 */
public class Env extends EnvTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  @Override
  public void render(TopologyContext context) {
    if (templateRef != null) {
      EnvTemplate template = context.resolveEnvTemplate(templateRef);
      copyFrom(template);
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (getName() == null) {
      throw new IllegalArgumentException("'name' attribute not specified on <env> element");
    }
    
    if (getRegions().isEmpty()) {
      throw new IllegalArgumentException("No <region> child element specified under <env> element " + getName());
    }
    
    if (getClusters().isEmpty()) {
      throw new IllegalArgumentException("No <cluster> child element specified under <env> element " + getName());
    }
    
    for (Region r : getRegions()) {
      r.validate();
    }
    
    for (Cluster c : getClusters()) {
      c.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreamable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("env");
    stream.attribute("name", getName());
    for (Region r : getRegions()) {
      r.output(stream);
    }
    for (Cluster c : getClusters()) {
      c.output(stream);
    }
    stream.endElement("env");
  }

}
