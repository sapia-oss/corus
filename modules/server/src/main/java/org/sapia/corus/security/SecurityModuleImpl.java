package org.sapia.corus.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.CorusSecurityException.Type;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.util.UriPattern;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link SecurityModule} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = SecurityModule.class)
@Remote(interfaces = { SecurityModule.class})
public class SecurityModuleImpl extends ModuleHelper implements SecurityModule, Interceptor {

  static final String PROPERTY_ALLOW = "corus.server.security.hostPattern.allow";
  static final String PROPERTY_DENY  = "corus.server.security.hostPattern.deny";
  
  private static final String LOCALHOST;
  static {
    String hostAddress = null;
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException uhe) {
      System.err.println("Unable to get the localhost address");
      uhe.printStackTrace();
    } finally {
      LOCALHOST = hostAddress;
    }
  }

  @Autowired
  private DbModule db;
  
  private DbMap<String, RoleConfig> roles;
  
  private Map<String, UriPattern> allowedPatterns = new ConcurrentHashMap<String, UriPattern>();
  private Map<String, UriPattern> deniedPatterns  = new ConcurrentHashMap<String, UriPattern>();

  private boolean isRunning = false;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setRoles(DbMap<String, RoleConfig> roles) {
    this.roles = roles;
  }
  
  Map<String, UriPattern> getAllowedPatterns() {
    return allowedPatterns;
  }
  
  Map<String, UriPattern> getDeniedPatterns() {
    return deniedPatterns;
  }
  
  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle

  @Override
  public void init() throws Exception {
    logger().info("Initializing the security module");
    roles = db.getDbMap(String.class, RoleConfig.class, "roles.configs");
    Hub.getModules().getServerRuntime().addInterceptor(ServerPreInvokeEvent.class, this);
    serverContext.getServices().getEventDispatcher().addInterceptor(PropertyChangeEvent.class, this);
  }
  
  @Override
  public void start() throws Exception {
    logger().info("Starting the security module");
    isRunning = true;
  }

  @Override
  public void dispose() {
    logger().info("Stopping the security module");
    isRunning = false;
  }
  
  // --------------------------------------------------------------------------
  // SecurityModule interface
  
  @Override
  public void addRole(String role, Set<Permission> permissions) throws CorusSecurityException {
    Assertions.isTrue(roles.get(role) == null, "Role already exists: %s", role);
    roles.put(role, new RoleConfig(role, permissions));
  }
  
  @Override
  public void addOrUpdateRole(String role, Set<Permission> permissions) {
    roles.put(role, new RoleConfig(role, permissions));
  }
  
  @Override
  public Set<Permission> getPermissionsFor(String role)
      throws CorusSecurityException {
    RoleConfig conf = roles.get(role);
    if (conf == null) {
      throw new CorusSecurityException("Role not found: " + role, Type.NO_SUCH_ROLE);
    }
    return conf.getPermissions();
  }
  
  @Override
  public List<RoleConfig> getRoleConfig(Arg role) {
    List<RoleConfig> roleInfos = new ArrayList<>();
    for (RoleConfig rc : roles) {
      if (role.matches(rc.getKey())) {
        roleInfos.add(rc);
      }
    }
    return roleInfos;
  }
  
  @Override
  public void removeRole(String role) throws IllegalArgumentException {
     RoleConfig rc = roles.get(role);
     if (rc == null) {
       throw new IllegalArgumentException("Role not found: " + role);
     }
     roles.remove(role);
  }
  
  @Override
  public void updateRole(String role, Set<Permission> permissions)
      throws IllegalArgumentException {
    RoleConfig rc = roles.get(role);
    if (rc == null) {
      throw new IllegalArgumentException("Role not found: " + role);
    }
    rc.setPermissions(permissions);
    rc.save();
  }
  
  // --------------------------------------------------------------------------
  // Config

  /**
   * Set the pattern list of the allowed hosts that can connect to this corus
   * server.
   * 
   * @param patternList
   *          The pattern list of allowed hosts.
   */
  public void setAllowedHostPatterns(String patternList) {
    doSetAllowedPatterns(patternList, PropertyChangeEvent.Type.ADD);
  }

  /**
   * Set the pattern list of the denied hosts that can't connect to this corus
   * server.
   * 
   * @param patternList
   *          The pattern list of denied hosts.
   */
  public void setDeniedHostPatterns(String patternList) {
    doSetDeniedPatterns(patternList, PropertyChangeEvent.Type.ADD);
  }
  
  // --------------------------------------------------------------------------
  // Interceptor methods
  
  /**
   * @param event
   *          a {@link PropertyChangeEvent}.
   */
  public void onPropertyChangeEvent(PropertyChangeEvent event) {
    if (event.getScope() == PropertyScope.SERVER) {
      if (event.getName().equals(PROPERTY_ALLOW)) {
        logger().debug("Got property change notification: " + PROPERTY_ALLOW);
        doSetAllowedPatterns(event.getValue(), event.getType());
      } else if (event.getName().equals(PROPERTY_DENY)) {
        logger().debug("Got property change notification: " + PROPERTY_DENY);
        doSetDeniedPatterns(event.getValue(), event.getType());
      }
    }
  }
  
  /**
   * 
   * @param evt
   *          a {@link ServerPreInvokeEvent}
   */
  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt) {
    if (!isRunning) {
      throw new IllegalStateException("This security module is not currently running");
    }

    TCPAddress addr = (TCPAddress) evt.getInvokeCommand().getConnection().getServerAddress();
    if (!isMatch(addr.getHost())) {
      logger().error("Security breach; could not execute: " + evt.getInvokeCommand().getMethodName());
      throw new CorusSecurityException("Host does not have access to corus server: " + addr, Type.HOST_NOT_AUTHORIZED);
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted

  protected boolean isMatch(String hostAddr) {
    if (allowedPatterns.size() == 0 && deniedPatterns.size() == 0) {
      return true;
    }

    boolean isMatching = (allowedPatterns.size() == 0);
    for (UriPattern pattern : allowedPatterns.values()) {
      if (pattern.matches(hostAddr)) {
        isMatching = true;
        break;
      }
    } 
    for (UriPattern pattern : deniedPatterns.values()) {
      if (pattern.matches(hostAddr)) {
        isMatching = false;
        break;
      }
    } 
    return isMatching;
  }
  
  private void doSetAllowedPatterns(String patternList, PropertyChangeEvent.Type eventType) {
    if (!Strings.isBlank(patternList)) {
      String[] patterns = StringUtils.split(patternList, ',');
      for (int i = 0; i < patterns.length; i++) {
        String pattern = patterns[i].trim();
        if (pattern.equals("localhost")) {
          if (eventType == PropertyChangeEvent.Type.ADD) {
            log.debug("Adding ALLOW pattern: " + pattern);
            allowedPatterns.put(pattern, UriPattern.parse(LOCALHOST));
          } else {
            log.debug("Removing ALLOW pattern: " + pattern);
            allowedPatterns.remove(pattern);
          }
        } else {
          if (eventType == PropertyChangeEvent.Type.ADD) {
            log.debug("Adding ALLOW pattern: " + pattern);
            allowedPatterns.put(pattern, UriPattern.parse(pattern));
          } else {
            log.debug("Removing ALLOW pattern: " + pattern);
            allowedPatterns.remove(pattern);
          }

        }
      }
    }    
  }
  
  private void doSetDeniedPatterns(String patternList, PropertyChangeEvent.Type eventType) {
    if (!Strings.isBlank(patternList)) {
      String[] patterns = StringUtils.split(patternList, ',');
      for (int i = 0; i < patterns.length; i++) {
        String pattern = patterns[i].trim();
        if (pattern.equals("localhost")) {
          if (eventType == PropertyChangeEvent.Type.ADD) {
            log.debug("Adding DENY pattern: " + pattern);
            deniedPatterns.put(pattern, UriPattern.parse(LOCALHOST));
          } else {
            log.debug("Removing DENY pattern: " + pattern);
            deniedPatterns.remove(pattern);
          }
        } else {
          if (eventType == PropertyChangeEvent.Type.ADD) {
            log.debug("Adding DENY pattern: " + pattern);
            deniedPatterns.put(pattern, UriPattern.parse(pattern));
          } else {
            log.debug("Removing DENY pattern: " + pattern);
            deniedPatterns.remove(pattern);
          }
        }
      }
    }    
  }
}
