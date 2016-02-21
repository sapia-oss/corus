package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Strings;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Holds Amazon ELB publishing config.
 * 
 * @author yduchesne
 *
 */
public class AwsElbPublisherConfig implements ProcessPubConfig, Externalizable, ObjectCreationCallback  {
  
  public static final String ELEMENT_NAME = "aws-elb-publisher";
  public static final int DEFAULT_MAX_UNPUBLISH_ATTEMPTS  = 20;
  public static final int DEFAULT_UNPUBLISH_INTERVAL      = 5;
  
  public static final int DEFAULT_MAX_PUBLISH_ATTEMPTS    = 20;
  public static final int DEFAULT_PUBLISH_INTERVAL        = 5;
  
  private String elbName;
  private int    maxUnpublishAttempts = DEFAULT_MAX_UNPUBLISH_ATTEMPTS;
  private int    unpublishInterval    = DEFAULT_UNPUBLISH_INTERVAL;
  private int    maxPublishAttempts   = DEFAULT_MAX_PUBLISH_ATTEMPTS;
  private int    publishInterval      = DEFAULT_PUBLISH_INTERVAL;
  
  public void setElbName(String elbName) {
    this.elbName = elbName;
  }
  
  public String getElbName() {
    return elbName;
  }
  
  public void setMaxUnpublishAttempts(int maxUnpublishAttempts) {
    this.maxUnpublishAttempts = maxUnpublishAttempts;
  }
  
  public int getMaxUnpublishAttempts() {
    return maxUnpublishAttempts;
  }
  
  public void setUnpublishInterval(int unpublishInterval) {
    this.unpublishInterval = unpublishInterval;
  }
  
  public int getUnpublishInterval() {
    return unpublishInterval;
  }
  
  public void setMaxPublishAttempts(int maxPublishAttempts) {
    this.maxPublishAttempts = maxPublishAttempts;
  }
  
  public int getMaxPublishAttempts() {
    return maxPublishAttempts;
  }
  
  public void setPublishInterval(int publishInterval) {
    this.publishInterval = publishInterval;
  }
  
  public int getPublishInterval() {
    return publishInterval;
  }
  
  // --------------------------------------------------------------------------
  // ObjectCreationCallback interface
  
  public Object onCreate() throws ConfigurationException {
    attributeNotNullOrEmpty(ELEMENT_NAME, "elbName", elbName);
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return elbName.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AwsElbPublisherConfig) {
      return ((AwsElbPublisherConfig) obj).elbName.equals(elbName);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "elbName", elbName);
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    elbName              = (String) in.readObject();
    maxUnpublishAttempts = in.readInt();
    unpublishInterval    = in.readInt();
    maxPublishAttempts   = in.readInt();
    publishInterval      = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(elbName);
    out.writeInt(maxUnpublishAttempts);
    out.writeInt(unpublishInterval);
    out.writeInt(maxPublishAttempts);
    out.writeInt(publishInterval);
  }
}
