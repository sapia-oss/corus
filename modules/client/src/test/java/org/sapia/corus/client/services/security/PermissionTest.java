package org.sapia.corus.client.services.security;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.util.Collects;

public class PermissionTest {

  @Test
  public void testForAbbreviation() {
    for (Permission p : Permission.values()) {
      Permission.forAbbreviation(p.abbreviation());
    }
  }

  @Test
  public void testForPermissionSet() {
    Set<Permission> perms = Permission.forPermissionSet("rwxd");
    assertTrue(perms.containsAll(Collects.arrayToSet(Permission.READ, Permission.WRITE, Permission.EXECUTE, Permission.DEPLOY)));
  }

}
