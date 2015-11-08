package org.sapia.corus.aws;

import com.amazonaws.auth.AWSCredentials;

/**
 * Specifies the interface for retrieving AWS-related configuration.
 * 
 * @author yduchesne
 *
 */
public interface AwsConfiguration {
  
  public interface AwsConfigChangeListener {
    
    public void onAwsEnabled();
    
    public void onAwsDisabled();
    
  }

  /**
   * @return the EC2 instance ID.
   * 
   * @throws IllegalStateException if AWS support is disabled.
   * 
   * @see #isAwsEnabled()
   */
  public String getInstanceId() throws IllegalStateException;
  
  /**
   * @return <code>true</code> if AWS support is enabled.
   */
  public boolean isAwsEnabled();
  
  /**
   * @return the {@link AWSCredentials}.
   */
  public AWSCredentials getCredentials();
  
  /**
   * @param listener an {@link AwsConfigChangeListener}.
   */
  public void addConfigChangeListener(AwsConfigChangeListener listener);
}
