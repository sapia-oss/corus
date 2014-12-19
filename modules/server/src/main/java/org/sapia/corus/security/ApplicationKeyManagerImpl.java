package org.sapia.corus.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.security.AppSubject;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.CorusSecurityException.Type;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.client.services.security.Subject;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link ApplicationKeyManager} interface.
 * 
 * @author yduchesne
 */
@Bind(moduleInterface = { ApplicationKeyManager.class })
@Remote(interfaces = { ApplicationKeyManager.class })
public class ApplicationKeyManagerImpl extends ModuleHelper implements ApplicationKeyManager {
    
  @Autowired
  private SecurityModule              securityModule;
  
  @Autowired
  private DbModule                    db;
  
  private DbMap<String, AppKeyConfig> appKeys;
  
  // --------------------------------------------------------------------------
  // Visible for testing
 
  void setSecurityModule(SecurityModule securityModule) {
    this.securityModule = securityModule;
  }

  void setAppKeys(DbMap<String, AppKeyConfig> appKeys) {
    this.appKeys = appKeys;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    appKeys = db.getDbMap(String.class, AppKeyConfig.class, "appkeys.configs");
  }
  
  @Override
  public void dispose() throws Exception {
  }
 
  // --------------------------------------------------------------------------
  // Module interface 
  
  @Override
  public String getRoleName() {
    return ApplicationKeyManager.ROLE;
  }
  
  // --------------------------------------------------------------------------
  // ApplicationKeyModule interface 
  
  @Override
  public Subject authenticate(String appId, String appKey)
      throws CorusSecurityException {
    Assertions.isFalse(Strings.isBlank(appId), "Application ID must be specified");
    Assertions.isFalse(Strings.isBlank(appKey), "Application key must be specified");
    AppKeyConfig config = appKeys.get(appId);
    if (config == null) {
      throw new CorusSecurityException("Invalid application ID or application key", Type.INVALID_APP_ID_OR_KEY);
    }
    return new AppSubject(appId, config.getRole(), securityModule.getPermissionsFor(config.getRole()));
  }  

  @Override
  public void changeApplicationKey(String appId, String newAppkey)
      throws IllegalArgumentException {
    AppKeyConfig config = appKeys.get(appId);
    Assertions.notNull(config, "Unknown application ID");
    config.setApplicationKey(newAppkey);
    config.save();
  }

  @Override
  public void changeRole(String appId, String role)
      throws IllegalArgumentException, CorusSecurityException {
    AppKeyConfig config = appKeys.get(appId);
    Assertions.notNull(config, "Unknown application ID");
    // validating role's existence
    securityModule.getPermissionsFor(role);
    // changing role
    config.setRole(role);
    config.save();
  }
  
  @Override
  public void addApplicationKey(String appId, String role, String newAppKey)
      throws IllegalArgumentException, CorusSecurityException {
    Assertions.isFalse(Strings.isBlank(appId), "Application ID must be specified");
    Assertions.isFalse(Strings.isBlank(role), "Role must be specified");
        
    // validating role's existence
    securityModule.getPermissionsFor(role);
    
    // making sure that app ID does not already exist
    Assertions.isTrue(appKeys.get(appId) == null, "Application key already exists for: %s", appId);
    
    // creating
    AppKeyConfig config = new AppKeyConfig(appId, role, newAppKey);
    appKeys.put(appId, config);
  }
  
  @Override
  public void addOrUpdateApplicationKey(String appId, String appKey, String role) {
    Assertions.isFalse(Strings.isBlank(appId), "Application ID must be specified");
    Assertions.isFalse(Strings.isBlank(role), "Role must be specified");
        
    // validating role's existence
    securityModule.getPermissionsFor(role);
    
    // creating
    AppKeyConfig config = new AppKeyConfig(appId, role, appKey);
    appKeys.put(appId, config);
  }
  
  @Override
  public void removeAppKey(Arg appId) {
    Set<String> toRemove = new HashSet<>();
    for(AppKeyConfig apk : appKeys) {
      if (appId.matches(apk.getKey())) {
        toRemove.add(apk.getKey());
      }
    }
    for (String r : toRemove) {
      appKeys.remove(r);
    }
  }
 
  @Override
  public List<AppKeyConfig> getAppKeyConfig(Arg appId) {
    List<AppKeyConfig> toReturn = new ArrayList<>();
    for(AppKeyConfig apk : appKeys) {
      if (appId.matches(apk.getKey())) {
        toReturn.add(apk);
      }
    }
    return toReturn;
  }
}
