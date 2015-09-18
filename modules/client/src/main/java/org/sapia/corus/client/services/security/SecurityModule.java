package org.sapia.corus.client.services.security;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;

/**
 * @author Yanick Duchesne
 */
public interface SecurityModule extends java.rmi.Remote, Module, Dumpable {

  /**
   * Encapsulates a role-to-permission association.
   * 
   * @author yduchesne
   *
   */
  public static class RoleConfig extends AbstractPersistent<String, RoleConfig> 
    implements Externalizable, Comparable<RoleConfig>, Matcheable, JsonStreamable, Mappable {
    
    static final long serialVersionUID = 1L;

    static final int VERSION_1       = 1;
    static final int CURRENT_VERSION = VERSION_1;
        
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
      
      int inputVersion = in.readInt();
      if (inputVersion == VERSION_1) {
        role = in.readUTF();
        permissions = (Set<Permission>) in.readObject();
      } else {
        throw new IllegalStateException("Version not handled: " + inputVersion);
      }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
      
      out.writeInt(CURRENT_VERSION);
      
      out.writeUTF(role);
      out.writeObject(permissions);
    }
    
    // ------------------------------------------------------------------------
    // JsonStreamable interface
    
    @Override
    public void toJson(JsonStream stream, ContentLevel level) {
      stream.beginObject()
        .field("classVersion").value(CURRENT_VERSION)
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
    
    public static RoleConfig fromJson(JsonInput in) {
      int classVersion = in.getInt("classVersion");
      if (classVersion == VERSION_1) {
        Set<Permission> permissions = new HashSet<>();
        for (JsonInput jsonPerm : in.iterate("permissions")) {
          permissions.add(Permission.forAbbreviation(jsonPerm.getString("abbreviation").charAt(0)));
        }
        
        RoleConfig conf = new RoleConfig(
            in.getString("name"),
            permissions
        );
        return conf;
      } else {
        throw new IllegalStateException("Version not handled: " + classVersion);
      }
    }
    
    @Override
    public Map<String, Object> asMap() {
      Map<String, Object> toReturn = new HashMap<>();
      toReturn.put("role.name", role);
      toReturn.put("role.permissions", permissions);
      return toReturn;
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
   * @param role an {@link ArgMatcher} instance corresponding to the role(s) to match.
   * @return the {@link List} of {@link RoleConfig} instances corresponding to the roles
   * currently configured.
   */
  public List<RoleConfig> getRoleConfig(ArgMatcher role);
  
  /**
   * @param role the name of a role to remove.
   * @throws IllegalArgumentException when no such role exists.
   */
  public void removeRole(String role) throws IllegalArgumentException;

  /**
   * @param rolePattern an {@link ArgMatcher} instance corresponding to the roles to remove.
   */
  public void removeRole(ArgMatcher rolePattern);
}
