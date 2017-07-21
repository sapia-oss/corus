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
   * @return the current EC2 instance ID.
   * 
   * @throws IllegalStateException if AWS support is disabled or if the instance ID
   *         could not be determined.
   * @see #isAwsEnabled()
   */
  public String getInstanceId() throws IllegalStateException;
  
  /**
   * @return the current AWS availability zone.
   * 
   * @throws IllegalStateException if AWS support is disabled or if the AZ
   *         could not be determined.
   * @see #isAwsEnabled()
   */
  public String getAvailabilityZone() throws IllegalStateException;
  
  /**
   * @return the current AWS region.
   * 
   * @throws IllegalStateException if AWS support is disabled or if the AWS region
   *         could not be determined.
   * @see #isAwsEnabled()
   */
  public String getRegion() throws IllegalStateException;
  
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
