package org.sapia.corus.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationKeyManagerImplTest {

  @Mock
  private SecurityModule security;

  @Mock
  private DbMap<String, AppKeyConfig> appKeys;
  
  private AppKeyConfig config;
 
  private ApplicationKeyManagerImpl manager;
  
  @Before
  public void setUp() {
    manager = new ApplicationKeyManagerImpl();
    manager.setAppKeys(appKeys);
    manager.setSecurityModule(security);
    
    config = new AppKeyConfig("test-app", "admin", "1234");
    config.setDbMap(appKeys);
    
    when(appKeys.get("test-app")).thenReturn(config);
    when(security.getPermissionsFor("admin")).thenReturn(Collects.arrayToSet(Permission.values()));
    when(security.getPermissionsFor("guest")).thenReturn(Collects.arrayToSet(Permission.READ));
    
    when(appKeys.iterator()).thenReturn(Collects.arrayToList(config).iterator());
  }
  
  @Test
  public void testAuthenticate() {
    manager.authenticate("test-app", "1234");
  }
  
  @Test(expected = CorusSecurityException.class)
  public void testAuthenticate_app_id_invalid() {
    manager.authenticate("test-app2", "1234");
  }

  @Test
  public void testChangeApplicationKey() {
    manager.changeApplicationKey("test-app", "4567");
    
    assertEquals("4567", config.getApplicationKey());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testChangeApplicationKey_app_id_invalid() {
    manager.changeApplicationKey("test-app2", "4567");
    
    assertEquals("4567", config.getApplicationKey());
  }

  @Test
  public void testChangeRole() {
    manager.changeRole("test-app", "guest");
    
    assertEquals("guest", config.getRole());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testChangeRole_app_id_invalid() {
    manager.changeRole("test-app2", "guest");
    
    assertEquals("guest", config.getRole());
  }

  @Test
  public void testAddApplicationKey() {
    manager.addApplicationKey("test-app2", "admin", "abcd");
    
    verify(appKeys).put(eq("test-app2"), any(AppKeyConfig.class));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAddApplicationKey_already_exists() {
    manager.addApplicationKey("test-app", "admin", "abcd");
  }

  @Test
  public void testAddOrUpdateApplicationKey_add() {
    manager.addApplicationKey("test-app2", "admin", "abcd");
    
    verify(appKeys).put(eq("test-app2"), any(AppKeyConfig.class));
  }
  
  @Test
  public void testAddOrUpdateApplicationKey_update() {
    manager.addApplicationKey("test-app2", "admin", "abcd");
    
    verify(appKeys).put(eq("test-app2"), any(AppKeyConfig.class));
  }

  @Test
  public void testRemoveAppKey() {
    manager.removeAppKey(ArgFactory.exact("test-app"));
    
    verify(appKeys).remove("test-app");
  }

  @Test
  public void testRemoveAppKey_app_id_not_found() {
    manager.removeAppKey(ArgFactory.exact("test-app2"));
    
    verify(appKeys, never()).remove("test-app");
  }
}
