package org.sapia.corus.client.services.security;

import java.util.Set;

import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.ubik.util.Strings;

/**
 * Models an application subject.
 * 
 * @author yduchesne
 *
 */
public class AppSubject implements Subject {

  private String          appId;
  private String          role;
  private Set<Permission> permissions;
  
  /**
   * @param appId this instance's corresponding application ID.
   * @param role the role assigned to this instance.
   * @param perms the {@link Set} of {@link Permission}s assigned to the application.
   */
  public AppSubject(String appId, String role, Set<Permission> perms) {
    this.appId        = appId;
    this.role         = role;
    this.permissions  = perms;
  }
  
  // --------------------------------------------------------------------------
  // Subject interface impl.

  @Override
  public boolean hasPermission(Permission p) {
    return permissions.contains(p);
  }
  
  @Override
  public boolean hasPermissions(Set<Permission> perms) {
    return permissions.containsAll(perms);
  }
  
  @Override
  public boolean isAnonymous() {
    return false;
  }
  
  @Override
  public AuditInfo getAuditInfo() {
    return AuditInfo.forClientId(appId);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AppSubject) {
      return ((AppSubject) obj).appId.equals(appId);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return appId.hashCode();
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "appId", appId, "role", role, "permissions", permissions);
  }
}
