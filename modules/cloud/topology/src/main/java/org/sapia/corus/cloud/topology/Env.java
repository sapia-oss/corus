package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>env</code> element.
 * 
 * @author yduchesne
 *
 */
public class Env extends EnvTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  private Topology topology;
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  void setTopology(Topology topology) {
    setParent(topology);
    this.topology = topology;
  }
  
  public Topology getTopology() {
    return topology;
  }
  
  @Override
  public void render(TopologyContext context) {
    if (templateRef != null) {
      EnvTemplate template = context.resolveEnvTemplate(templateRef);
      copyFrom(template);
    }
    
    for (Region r : getRegions()) {
      r.render(context);
    }
    
    for (Cluster c : getClusters()) {
      c.render(context);
    }
  }
  
  @Override
  public void addRegion(Region r) {
    r.setEnv(this);
    super.addRegion(r);
  }
  
  @Override
  public void addCluster(Cluster c) {
    super.addCluster(c);
    c.setEnv(this);
  }
  
  @Override
  public void copyFrom(EnvTemplate other) {
    super.copyFrom(other);
    for (Region r : getRegions()) {
      r.setEnv(this);
    }
    for (Cluster c : getClusters()) {
      c.setEnv(this);
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
