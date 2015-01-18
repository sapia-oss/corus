package org.sapia.corus.client.services.security;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;

/**
 * @author Yanick Duchesne
 */
public interface SecurityModule extends java.rmi.Remote, Module {

  /**
   * Encapsulates a role-to-permission association.
   * 
   * @author yduchesne
   *
   */
  public static class RoleConfig extends AbstractPersistent<String, RoleConfig> 
    implements Externalizable, Comparable<RoleConfig>, Matcheable, JsonStreamable {
    
    static final long serialVersionUID = 1L;
    
    private String          role;
    private Set<Permission> permissions;
    
    /**
     * DO NOT USE: meant for externalization.
     */
    public RoleConfig() {
    }
    
    public RoleConfig(String role, Set<Permission> permissions) {
      this.role        = role;
      this.permissions = permissions;
    }
    
    @Override
    public String getKey() {
      return role;
    }
    
    public String getRole() {
      return role;
    }
    
    public Set<Permission> getPermissions() {
      return permissions;
    }
    
    public void setPermissions(Set<Permission> permissions) {
      this.permissions = permissions;
    }
    
    // ------------------------------------------------------------------------
    // Comparable interface
    
    public int compareTo(RoleConfig o) {
      return role.compareTo(o.role);
    }
    
    @Override
    public boolean matches(Pattern pattern) {
      return pattern.matches(role);
    }
    
    // ------------------------------------------------------------------------
    // Externalizable interface
    
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      role = in.readUTF();
      permissions = (Set<Permission>) in.readObject();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeUTF(role);
      out.writeObject(permissions);
    }
    
    // ------------------------------------------------------------------------
    // JsonStreamable interface
    
    @Override
    public void toJson(JsonStream stream) {
      stream.beginObject()
        .field("name").value(role)
        .field("permissions");
      
      stream.beginArray();
        for (Permission p : permissions) {
          stream.beginObject()
            .field("name").value(p.name())
            .field("abbreviation").value("" + p.abbreviation())
          .endObject();
        }
      stream.endArray();
      
      stream.endObject();
    }
  }
  
  // ==========================================================================
  
  /** Defines the role name of this module. */
  public static final String ROLE = SecurityModule.class.getName();

  /**
   * @param role the name of the role whose permissions should be returned.
   * @return the {@link Set} of {@link Permission}s corresponding to the given role.
   * @throws CorusSecurityException if no such rule is defined.
   */
  public Set<Permission> getPermissionsFor(String role) throws CorusSecurityException;
  
  /**
   * @param role the name of the role to add.
   * @param permissions the {@link Set} of {@link Permission}s to assign to the role.
   * @throws IllegalArgumentException if a role with such a name is already defined.
   */
  public void addRole(String role, Set<Permission> permissions) throws IllegalArgumentException;
  
  /**
   * @param role the name of the role to add/update.
   * @param permissions the {@link Set} of {@link Permission}s to assign to the role.
   */
  public void addOrUpdateRole(String role, Set<Permission> permissions);
  
  /**
   * @param role the name of the role to update.
   * @param permissions the {@link Set} of {@link Permission}s to assign to the role.
   * @throws IllegalArgumentException if no such role exists.
   */
  public void updateRole(String role, Set<Permission> permissions) throws IllegalArgumentException;
  
  /**
   * @param role an {@link Arg} instance corresponding to the role(s) to match.
   * @return the {@link List} of {@link RoleConfig} instances corresponding to the roles
   * currently configured.
   */
  public List<RoleConfig> getRoleConfig(Arg role);
  
  /**
   * @param role the name of a role to remove.
   * @throws IllegalArgumentException when no such role exists.
   */
  public void removeRole(String role) throws IllegalArgumentException;

  /**
   * @param rolePattern an {@link Arg} instance corresponding to the roles to remove.
   */
  public void removeRole(Arg rolePattern);
}
