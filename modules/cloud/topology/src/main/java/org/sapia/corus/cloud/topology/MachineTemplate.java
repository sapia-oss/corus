package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>machine-template</code> element.
 * 
 * @author yduchesne
 *
 */
public class MachineTemplate extends ParamContainer {
  
  private String name, imageId;
  
  private Set<ServerTag> serverTags = new HashSet<>();
  
  private PropertyCollection serverProperties, processProperties;
  
  private int minInstances, maxInstances = -1;

  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setMinInstances(int minInstances) {
    this.minInstances = minInstances;
  }
  
  public int getMinInstances() {
    return minInstances;
  }
  
  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
  }
  
  public int getMaxInstances() {
    return maxInstances;
  }
  
  public void setImageId(String imageId) {
    this.imageId = imageId;
  }
  
  public String getImageId() {
    return imageId;
  }
  
  public void setServerTags(String tagsList) {
    for (String t : tagsList.split(",")) {
      String tagValue = t.trim();
      if (tagValue.length() > 0) {
        ServerTag tag = new ServerTag();
        tag.setValue(tagValue);
        serverTags.add(tag);
      }
    }
  }
  
  public Set<ServerTag> getServerTags() {
    return serverTags;
  }
  
  public void addServerTag(ServerTag t) {
    serverTags.add(t);
  }
  
  public void setServerProperties(PropertyCollection propertyCollection) {
    this.serverProperties = propertyCollection;
  }
  
  public void setProcessProperties(PropertyCollection processProperties) {
    this.processProperties = processProperties;
  }
  
  
  public PropertyCollection getServerProperties() {
    return createServerProperties();
  }
  
  public PropertyCollection getProcessProperties() {
    return createProcessProperties();
  }
  
  public PropertyCollection createServerProperties() {
    return serverProperties == null ? serverProperties = new PropertyCollection() : serverProperties;
  }
  
  public PropertyCollection createProcessProperties() {
    return processProperties == null ? processProperties = new PropertyCollection() : processProperties;
  }
  
  public void copyFrom(MachineTemplate other) {
    this.processProperties.copyFrom(other.processProperties);
    this.serverProperties.copyFrom(other.serverProperties);
    this.serverTags.addAll(other.serverTags);
    
    if (minInstances < 0) {
      minInstances = other.minInstances;
    }
    if (maxInstances < 0) {
      maxInstances = other.maxInstances;
    }
    if (imageId == null) {
      this.imageId = other.imageId;
    }
    addParams(other.getParams());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MachineTemplate) {
      MachineTemplate other = (MachineTemplate) obj;
      if (name == null || other.name == null) {
        return false;
      }
      return name.equals(other.name);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (name == null) {
      return super.hashCode();
    } else {
      return name.hashCode();
    }
  }
}
