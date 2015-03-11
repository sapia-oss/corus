package org.sapia.corus.client.services.security;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.ubik.util.Strings;

/**
 * Specifies the behavior for managing application keys.
 * 
 * @author yduchesne
 *
 */
public interface ApplicationKeyManager extends java.rmi.Remote, Module {
  
  /**
   * Holds an application key's corresponding data.
   * 
   * @author yduchesne
   *
   */
  public static class AppKeyConfig extends AbstractPersistent<String, AppKeyConfig> 
    implements Externalizable, Comparable<AppKeyConfig>, Matcheable, JsonStreamable {
    
    static final long serialVersionUID = 1L;
    
    private String          appId;
    private String          role;
    private String          applicationKey;
    
    /**
     * DO NOT USE: meant for externalization only.
     */
    public AppKeyConfig() {
    }
    
    public AppKeyConfig(String appId, String role, String appKey) {
      this.appId          = appId;
      this.role           = role;
      this.applicationKey = appKey;
    }
    
    @Override
    public String getKey() {
      return appId;
    }
    
    public String getAppId() {
      return appId;
    }
    
    public String getApplicationKey() {
      return applicationKey;
    }
    
    public void setApplicationKey(String applicationKey) {
      this.applicationKey = applicationKey;
    }
    
    public String getRole() {
      return role;
    }
    
    public void setRole(String role) {
      this.role = role;
    }
    
    // ------------------------------------------------------------------------
    // Comparable interface
    
    public int compareTo(AppKeyConfig o) {
      return appId.compareTo(o.appId);
    }
    
    // ------------------------------------------------------------------------
    // Matcheable interface
    
    public boolean matches(Matcheable.Pattern pattern) {
      return pattern.matches(appId) || pattern.matches(role);
    }
    
    // ------------------------------------------------------------------------
    // Externalizable interface
    
    @Override
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      appId          = in.readUTF();
      role           = in.readUTF();
      applicationKey = in.readUTF();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeUTF(appId);
      out.writeUTF(role);
      out.writeUTF(applicationKey);
    }
    
    // ------------------------------------------------------------------------
    // JsonStreamable interface
    
    @Override
    public void toJson(JsonStream stream) {
      stream.beginObject()
        .field("appId").value(appId)
        .field("role").value(role)
        .field("key").value(applicationKey)
      .endObject();      
    }
    
    // ------------------------------------------------------------------------
    // Object overrides
    
    @Override
    public String toString() {
      // not returning application key to avoid logging it
      return Strings.toStringFor(this, "appId", appId, "role", role);
    }
  }
  
  // ==========================================================================

  /** Defines the role name of this module. */
  public static final String ROLE = ApplicationKeyManager.class.getName();
  
  /**
   * @param appId an {@link ArgMatcher} instance corresponding to one or more application keys to remove.
   */
  public void removeAppKey(ArgMatcher appId);

  /**
   * @param appId an application ID to which to associate an application key.
   * @param appKey the application key to associate to the given application ID.
   * @param role the name of the role to associate to the application key.
   * @throws IllegalArgumentException if an application key already exists for the given ID.
   * @throws CorusSecurityException if the role specified is invalid.
   */
  public void addApplicationKey(String appId, String appKey, String role) throws IllegalArgumentException, CorusSecurityException;

  /**
   * @param appId an application ID to which to associate an application key.
   * @param appKey the application key to associate to the given application ID.
   * @param role the name of the role to associate to the application key.
   * @throws CorusSecurityException if the role specified is invalid.
   */
  public void addOrUpdateApplicationKey(String appId, String appKey, String role);
  
  /**
   * @param appId an {@link ArgMatcher} instance corresponding to one or more application key information
   * to remove.
   * @return a {@link List} of {@link AppKeyConfig}s matching the given criterion.
   */
  public List<AppKeyConfig> getAppKeyConfig(ArgMatcher appId);

  /**
   * Changes the application key associated to an existing application ID.
   * 
   * @param appId the application ID for which to generate a new application key.
   * @param appKey a new application key.
   * @throws IllegalArgumentException if no such application ID exists.
   */
  public void changeApplicationKey(String appId, String appKey) throws IllegalArgumentException;

  /**
   * Changes the role associated to an existing application ID.
   * 
   * @param appId the application ID to which to associate a new role.
   * @param role the role to associate to the given application ID.
   * @throws IllegalArgumentException if no such application ID exists.
   * @throws CorusSecurityException if the role specified is invalid.
   */
  public void changeRole(String appId, String role) throws IllegalArgumentException, CorusSecurityException;
  
  /**
   * @param appId an application ID.
   * @param appKey an application key.
   * @return the {@link Subject} corresponding to the application that was authenticated.
   * @throws CorusSecurityException if the application could not be authenticated.
   */
  public Subject authenticate(String appId, String appKey) throws CorusSecurityException;

}

