package org.sapia.corus.cloud.topology;


/**
 * Holds Region data.
 * 
 * @author yduchesne
 *
 */
public class Region extends RegionTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  // --------------------------------------------------------------------------
  // TopologyElement
  
  @Override
  public void render(TopologyContext context) {
    if (templateRef != null) {
      copyFrom(context.resolveRegionTemplate(getTemplateRef()));
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (getName() == null) {
      throw new IllegalArgumentException("'name' not specified for <region> element");
    }
    
    for (Zone z : getZones()) {
      z.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XMLStreamable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("region");
    stream.attribute("name", getName());
    for (Zone z : getZones()) {
      z.output(stream);
    }
    stream.endElement("region");
  }

}
