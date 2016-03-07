package org.sapia.corus.cloud.topology;


/**
 * Holds Region data.
 * 
 * @author yduchesne
 *
 */
public class Region extends RegionTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  private Env env;
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  void setEnv(Env env) {
    setParent(env);
    this.env = env;
  }
  
  public Env getEnv() {
    return env;
  }
  
  @Override
  public void addZone(Zone z) {
    z.setRegion(this);
    super.addZone(z);
  }
  
  @Override
  public void copyFrom(RegionTemplate other) {
    super.copyFrom(other);
    for (Zone z : getZones()) {
      z.setRegion(this);
    }
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
