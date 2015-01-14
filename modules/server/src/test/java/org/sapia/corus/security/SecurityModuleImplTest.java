package org.sapia.corus.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.corus.util.UriPattern;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Localhost;

@RunWith(MockitoJUnitRunner.class)
public class SecurityModuleImplTest {

  @Mock
  private DbMap<String, RoleConfig> roles;
  
  private RoleConfig                config;
  
  private SecurityModuleImpl        mod;
  
  @Before
  public void setUp() {
    mod = new SecurityModuleImpl();
    mod.setRoles(roles);
    when(roles.keys()).thenReturn(Collects.arrayToList("admin").iterator());
    
    config = new RoleConfig("admin", Collects.arrayToSet(Permission.values()));
    config.setDbMap(roles);
    
    when(roles.get("admin")).thenReturn(config);
    when(roles.iterator()).thenReturn(Collects.arrayToList(config).iterator());
  }
  
  @Test
  public void testNoPatterns() {
    assertTrue(mod.isMatch("dummy"));
  }
  
  @Test
  public void testAllowedMatch() {
    mod.setAllowedHostPatterns("192.170.1.1, 192.168.**");
    boolean isAllowed = mod.isMatch("192.168.0.1");
    
    assertTrue("The hosts 192.168.0.1 should match the allowed pattern", isAllowed);
  }  

  @Test
  public void testAllowedNoMatch() {
    mod.setAllowedHostPatterns("192.170.1.1, 192.168.**");
    boolean isAllowed = mod.isMatch("200.168.0.1");
    
    assertTrue("The hosts 192.168.0.1 should not match the allowed pattern", !isAllowed);
  }
  
  @Test
  public void testDeniedMatch() {
    mod.setDeniedHostPatterns("192.170.1.1, 192.168.**");
    boolean isAllowed = mod.isMatch("192.168.0.1");
    
    assertTrue("The hosts 192.168.0.1 should match the denied pattern", !isAllowed);
  }  
  
  @Test
  public void testDeniedNoMatch() {
    mod.setDeniedHostPatterns("192.170.1.1, 192.168.**");
    boolean isAllowed = mod.isMatch("200.168.0.1");

    assertTrue("The hosts 200.168.0.1 should not match the denied pattern", isAllowed);
  }  

  @Test
  public void testComplexAllowedMatch() {
    mod.setAllowedHostPatterns("192.170.1.1, 192.168.**, 10.10.10.**");
    mod.setDeniedHostPatterns("200.170.1.1, 200.168.**, 10.10.10.10");

    boolean isAllowed = mod.isMatch("192.168.0.1");
    assertTrue("The hosts 192.168.0.1 should match the allowed pattern", isAllowed);
    isAllowed = mod.isMatch("10.10.10.1");
   
    assertTrue("The hosts 10.10.10.1 should match the allowed pattern", isAllowed);
  }  

  @Test
  public void testComplexDeniedMatch() {
    mod.setAllowedHostPatterns("192.170.1.1, 192.168.**, 10.10.10.**");
    mod.setDeniedHostPatterns("200.170.1.1, 200.168.**, 10.10.10.10");

    boolean isAllowed = mod.isMatch("200.168.0.1");
    assertTrue("The hosts 200.168.0.1 should match the denied pattern", !isAllowed);
    isAllowed = mod.isMatch("10.10.10.10");
    
    assertTrue("The hosts 10.10.10.10 should match the denied pattern", !isAllowed);
  }  

  @Test
  public void testAllowedLocalhost() throws Exception {
    mod.setAllowedHostPatterns("localhost");
    String localhost = Localhost.getPreferredLocalAddress().getHostAddress();
    boolean isAllowed = mod.isMatch(localhost);
    
    assertTrue("The localhost [" + localhost + "] should match the allowed pattern", isAllowed);
  }  

  @Test
  public void testDeniedLocalhost() throws Exception {
    mod.setDeniedHostPatterns("localhost");
    String localhost = Localhost.getPreferredLocalAddress().getHostAddress();
    boolean isAllowed = mod.isMatch(localhost);
    
    assertTrue("The localhost [" + localhost + "] should match the denied pattern", !isAllowed);
  }  

  @Test
  public void testOnPropertyChangeEvent_allow_add() {
    PropertyChangeEvent evt = new PropertyChangeEvent(SecurityModuleImpl.PROPERTY_ALLOW, "192.168.0.101", PropertyScope.SERVER, Type.ADD);
    mod.onPropertyChangeEvent(evt);
    
    assertEquals(1, mod.getAllowedPatterns().size());
  }
  
  @Test
  public void testOnPropertyChangeEvent_allow_remove() {
    mod.getAllowedPatterns().put("192.168.0.101", UriPattern.parse("192.168.0.101"));
    PropertyChangeEvent evt = new PropertyChangeEvent(SecurityModuleImpl.PROPERTY_ALLOW, "192.168.0.101", PropertyScope.SERVER, Type.REMOVE);
    mod.onPropertyChangeEvent(evt);
    
    assertEquals(0, mod.getAllowedPatterns().size());
  }
  
  @Test
  public void testOnPropertyChangeEvent_deny_add() {
    PropertyChangeEvent evt = new PropertyChangeEvent(SecurityModuleImpl.PROPERTY_DENY, "192.168.0.101", PropertyScope.SERVER, Type.ADD);
    mod.onPropertyChangeEvent(evt);
    
    assertEquals(1, mod.getDeniedPatterns().size());
  }
  
  @Test
  public void testOnPropertyChangeEvent_deny_remove() {
    mod.getAllowedPatterns().put("192.168.0.101", UriPattern.parse("192.168.0.101"));
    PropertyChangeEvent evt = new PropertyChangeEvent(SecurityModuleImpl.PROPERTY_DENY, "192.168.0.101", PropertyScope.SERVER, Type.REMOVE);
    mod.onPropertyChangeEvent(evt);
    
    assertEquals(0, mod.getDeniedPatterns().size());
  }
  
  @Test
  public void testAddRole() {
    mod.addRole("guest", Collects.arrayToSet(Permission.READ));
    
    verify(roles).put(eq("guest"), any(RoleConfig.class));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAddRole_already_exists() {
    mod.addRole("admin", Collects.arrayToSet(Permission.READ));
  }
  
  @Test
  public void testAddOrUpdateRole_already_exists() {
    mod.addOrUpdateRole("admin", Collects.arrayToSet(Permission.READ));
    
    verify(roles).put(eq("admin"), any(RoleConfig.class));
  }

  @Test
  public void testAddOrUpdateRole_new() {
    mod.addOrUpdateRole("guest", Collects.arrayToSet(Permission.READ));
    
    verify(roles).put(eq("guest"), any(RoleConfig.class));
  }

  @Test
  public void testUpdateRole() {
    mod.updateRole("admin", Collects.arrayToSet(Permission.READ));
    
    verify(roles).put(eq("admin"), any(RoleConfig.class));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateRole_not_found() {
    mod.updateRole("guest", Collects.arrayToSet(Permission.READ));
  }
  
  @Test
  public void testRemoveRole() {
    mod.removeRole("admin");
    
    verify(roles).remove("admin");
  }
  
  @Test
  public void testRemoveRole_with_pattern() {
    mod.removeRole(ArgFactory.parse("adm*"));
    
    verify(roles).remove("admin");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveRole_not_found() {
    mod.removeRole("guest");
  }
}
