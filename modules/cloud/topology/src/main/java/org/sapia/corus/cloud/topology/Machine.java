package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>machine</code> element.
 * 
 * @author yduchesne
 *
 */
public class Machine extends MachineTemplate implements TopologyElement, XmlStreamable, Validateable {
  
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
      MachineTemplate template = context.resolveMachineTemplate(templateRef);
      copyFrom(template);
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable

  @Override
  public void validate() throws IllegalArgumentException {
    if (getImageId() == null) {
      throw new IllegalArgumentException("attribute 'imageId' not specified on <machine> element: " + getName());
    }
    
    if (getMinInstances() < 0) {
      throw new IllegalArgumentException("attribute 'minInstances' not valid on <machine> element: " + getName() + ". Must be > 0");
    }
    
    if (getMaxInstances() < 0) {
      throw new IllegalArgumentException("attribute 'maxInstances' not valid on <machine> element: " + getName() + ". Must be > 0");
    }
    
    if (getMaxInstances() < getMinInstances()) {
      throw new IllegalArgumentException("attribute 'maxInstances' not valid on <machine> element: " + getName() + ". Must be >= minInstances");
    }
    
    for (ServerTag t : getServerTags()) {
      t.validate();
    }
    
    for (CorusProperty p : getServerProperties().getProperties()) {
      p.validate();
    }
    
    for (CorusProperty p : getProcessProperties().getProperties()) {
      p.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("machine");
    stream.attribute("name", getName());
    stream.attribute("imageId", getImageId());
    stream.attribute("minInstances", getMinInstances());
    stream.attribute("maxInstances", getMaxInstances());
    
    for (ServerTag t : getServerTags()) {
      t.output(stream);
    }
    
    stream.beginElement("serverProperties");
    for (CorusProperty p : getServerProperties().getProperties()) {
      p.output(stream);
    }
    stream.endElement("serverProperties");
    
    stream.beginElement("processProperties");
    for (CorusProperty p : getProcessProperties().getProperties()) {
      p.output(stream);
    }
    stream.endElement("processProperties");
    
    stream.endElement("machine");
  }
  

}
