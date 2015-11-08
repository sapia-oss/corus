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

    if (getInstanceType() == null) {
      throw new IllegalArgumentException("attribute 'instanceType' not specified on <machine> element: " + getName());
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
    
    for (Property p : getServerProperties().getProperties()) {
      p.validate();
    }
    
    for (Property p : getProcessProperties().getProperties()) {
      p.validate();
    }
    
    for (Artifact a : getArtifacts()) {
      a.validate();
    }
    
    for (LoadBalancerAttachment lb : getLoadBalancerAttachments()) {
      lb.validate();
    }
    
    if (getRepoRole() == null) {
      throw new IllegalArgumentException("attribute 'repoRole' not specified on <machine> element: " + getName());
    } else if (!getRepoRole().equals(REPO_ROLE_CLIENT) && !getRepoRole().equals(REPO_ROLE_SERVER)) {
      throw new IllegalArgumentException("invalid value for attribute 'repoRole' on <machine> element " + getName() 
          + ". Expected either " + REPO_ROLE_CLIENT + " or " + REPO_ROLE_SERVER + ". Got: " + getRepoRole());
    } else if (getRepoRole().equals(REPO_ROLE_SERVER) && getMinInstances() == 0) {
      throw new IllegalArgumentException("minInstances attribute on <machine> element " + getName()
          + " should be set to value larger than 0, since the repoRole attribute is set to " + REPO_ROLE_SERVER
          + ". Repo server must guarantee one instance running at all times");
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
    stream.attribute("publicIpEnabled", isPublicIpEnabled() ? "true" : "false");
    
    for (ServerTag t : getServerTags()) {
      t.output(stream);
    }
    
    stream.beginElement("serverProperties");
    for (Property p : getServerProperties().getProperties()) {
      p.output(stream);
    }
    stream.endElement("serverProperties");
    
    stream.beginElement("processProperties");
    for (Property p : getProcessProperties().getProperties()) {
      p.output(stream);
    }
    stream.endElement("processProperties");
    
    stream.endElement("machine");
    
    for (Artifact a : getArtifacts()) {
      a.output(stream);
    }
    
    for (LoadBalancerAttachment lb : getLoadBalancerAttachments()) {
      lb.output(stream);
    }
  }
  

}
