package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Implementations of this interface are meant to add command lines to the
 * {@link UserDataBuilder} passed through the {@link UserDataContext}.
 * 
 * @author yduchesne
 *
 */
public interface UserDataPopulator {
  
  /**
   * @param context the current {@link UserDataContext}.
   * @see UserDataContext#getUserData()
   */
  public void addTo(UserDataContext context);
  
}
