package org.sapia.corus.client.services.security;

import java.util.Set;

import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.ubik.util.Collects;

/**
 * Models a subject.
 * 
 * @author yduchesne
 *
 */
public interface Subject {
  
  /**
   * @param p a {@link Permission}.
   * @return <code>true</code> if this subject has the given permission.
   */
  public boolean hasPermission(Permission p);
  
  /**
   * @param perms a {@link Set} of {@link Permission}s.
   * @return <code>true</code> if this instance has all the given permissions.
   */
  public boolean hasPermissions(Set<Permission> perms);
  
  /**
   * @return <code>true</code> if this subject is anonymous.
   */
  public boolean isAnonymous();
  
  /**
   * @return a new {@link AuditInfo} for this instance.
   */
  public AuditInfo getAuditInfo();
  
  // ==========================================================================
  // Anonymous subject impl.
  
  public class Anonymous implements Subject {
   
    public static final String CLIENT_ID = "anonymous";
    
    private Set<Permission> permissions = Collects.arrayToSet(Permission.READ);
    
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
      return true;
    }
    
    /**
     * @return a new instance of this class.
     */
    public static Anonymous newInstance() {
      return new Anonymous();
    }
    
    @Override
    public AuditInfo getAuditInfo() {
      return AuditInfo.forClientId(CLIENT_ID);
    }
  }
}
