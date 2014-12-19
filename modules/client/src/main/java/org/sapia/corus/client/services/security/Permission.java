package org.sapia.corus.client.services.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sapia.ubik.util.Assertions;

/**
 * Holds constants corresponding to the different permissions.
 * 
 * @author yduchesne
 *
 */
public enum Permission {

  READ("ls, ps", 'r', 0),
  WRITE("conf add, conf del, port add, port del, ps -clean, etc.", 'w', 1),
  EXECUTE("exec, kill, suspend, resume, restart", 'x', 2),
  DEPLOY("deploy, undeploy", 'd', 3);
  
  private static final Map<Character, Permission> PERMISSIONS_BY_ABBREVIATION = new HashMap<>();
  
  static {
    for (Permission p : Permission.values()) {
      PERMISSIONS_BY_ABBREVIATION.put(p.abbreviation, p);
    }
  }
  
  private String description;
  private char   abbreviation;
  private int    displayOrder;
  
  private Permission(String description, char abbreviation, int displayOrder) {
    this.description  = description;
    this.abbreviation = abbreviation;
    this.displayOrder = displayOrder;
  }
  
  /**
   * @return this instance's abbreviation character. 
   */
  public char abbreviation() {
    return abbreviation;
  }
  
  /**
   * @return this instance's description.
   */
  public String description() {
    return this.description;
  }
  
  /**
   * @return the order in which this instance is to be displayed.
   */
  public int displayOrder() {
    return displayOrder;
  }
  
  /**
   * @param abbreviation identifies a desired permission.
   * @return the {@link Permission} for the given abbreviation.
   */
  public static Permission forAbbreviation(char abbreviation) {
    Permission p = PERMISSIONS_BY_ABBREVIATION.get(abbreviation);
    Assertions.isFalse(p == null, "Invalid permission: %s. Expected one of: %s", 
        abbreviation, PERMISSIONS_BY_ABBREVIATION.keySet());
    return p;
  }
  
  /**
   * @param permissionSet the {@link Set} of {@link Permission}s corresponding
   * to the passed in "permission set".
   * @return a {@link Set} of {@link Permission} instances.
   */
  public static Set<Permission> forPermissionSet(String permissionSet) {
    Set<Permission> permissions = new HashSet<>();
    for (int i = 0; i < permissionSet.length(); i++) {
      permissions.add(forAbbreviation(permissionSet.charAt(i)));
    }
    return permissions;
  }
}
