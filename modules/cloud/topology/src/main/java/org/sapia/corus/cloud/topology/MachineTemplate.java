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
  
  public static final String REPO_ROLE_CLIENT = "client";
  public static final String REPO_ROLE_SERVER = "server";
  
  private boolean publicIpEnabled;
  private boolean isSeedNode;
  private String name, imageId, instanceType;
  
  private String repoRole = REPO_ROLE_CLIENT;
  
  private Set<ServerTag>              serverTags = new HashSet<>();
  private Set<Artifact>               artifacts  = new HashSet<>();
  private Set<LoadBalancerAttachment> lbs        = new HashSet<>();
  private UserData                    userdata   = new UserData();
  
  private PropertyCollection serverProperties  = new PropertyCollection();
  private PropertyCollection processProperties = new PropertyCollection();
  
  private int minInstances, maxInstances = -1;

  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public String getAlphaNumericName() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isAlphabetic(c) || Character.isDigit(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
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
  
  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }
  
  public String getInstanceType() {
    return instanceType;
  }
  
  public void setPublicIpEnabled(boolean publicIpEnabled) {
    this.publicIpEnabled = publicIpEnabled;
  }
  
  public boolean isPublicIpEnabled() {
    return publicIpEnabled;
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
    return serverProperties;
  }
  
  public PropertyCollection createProcessProperties() {
    return processProperties;
  }
  
  public Set<Artifact> getArtifacts() {
    return artifacts;
  }
  
  public void addArtifact(Artifact artifact) {
    artifacts.add(artifact);
  }
  
  public Set<LoadBalancerAttachment> getLoadBalancerAttachments() {
    return lbs;
  }
  
  public void addLoadBalancerAttachment(LoadBalancerAttachment lb) {
    lbs.add(lb);
  }
  
  public void setRepoRole(String repoRole) {
    this.repoRole = repoRole;
  }
  
  public String getRepoRole() {
    return repoRole;
  }
  
  public void setUserData(UserData userdata) {
    this.userdata = userdata;
  }
  
  public UserData getUserData() {
    return userdata;
  }

  public boolean isSeedNode() {
    return isSeedNode;
  }
  
  public void setSeedNode(boolean isSeedNode) {
    this.isSeedNode = isSeedNode;
  }
  
  public void copyFrom(MachineTemplate other) {
    this.processProperties.copyFrom(other.processProperties);
    this.serverProperties.copyFrom(other.serverProperties);
    this.serverTags.addAll(other.serverTags);
    this.artifacts.addAll(other.artifacts);
    this.lbs.addAll(other.lbs);
    this.userdata.copyFrom(other.userdata);
    this.publicIpEnabled = other.publicIpEnabled;
    if (minInstances < 0) {
      minInstances = other.minInstances;
    }
    if (maxInstances < 0) {
      maxInstances = other.maxInstances;
    }
    if (imageId == null) {
      this.imageId = other.imageId;
    }
    if (instanceType == null) {
      this.instanceType = other.instanceType;
    }
    if (!this.isSeedNode) {
      this.isSeedNode = other.isSeedNode;
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
