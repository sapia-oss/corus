package org.sapia.corus.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.util.UriPattern;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;

/**
 * Implements the {@link SecurityModule} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface=SecurityModule.class)
public class SecurityModuleImpl extends ModuleHelper implements SecurityModule, Interceptor{

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
  
  private List<UriPattern> allowedPatterns = new ArrayList<UriPattern>();
  private List<UriPattern> deniedPatterns = new ArrayList<UriPattern>();
  
  private boolean isRunning = false;
  
  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return ROLE;
  }
  
  public boolean isRunning() {
    return isRunning;
  }
  
  /**
   * Set the pattern list of the allowed hosts that can connect to
   * this corus server.
   * 
   * @param patternList The pattern list of allowed hosts.
   */  
  public synchronized void setAllowedHostPatterns(String patternList) {
    allowedPatterns.clear();
    
    if (patternList != null && (patternList = patternList.trim()).length() > 0) {
      String[] patterns = StringUtils.split(patternList, ',');
      for (int i = 0; i < patterns.length; i++) {
        if (patterns[i].trim().equals("localhost")) {
        	allowedPatterns.add(UriPattern.parse(LOCALHOST));
        } else {
          allowedPatterns.add(UriPattern.parse(patterns[i].trim()));
        }
      }
    }
  }
  
  /**
   * Set the pattern list of the denied hosts that can't connect to
   * this corus server.
   * 
   * @param patternList The pattern list of denied hosts.
   */  
  public synchronized void setDeniedHostPatterns(String patternList) {
    deniedPatterns.clear();
    
    if (patternList != null && (patternList = patternList.trim()).length() > 0) {
      String[] patterns = StringUtils.split(patternList, ',');
      for (int i = 0; i < patterns.length; i++) {
        if (patterns[i].trim().equals("localhost")) {
          deniedPatterns.add(UriPattern.parse(LOCALHOST));
        } else {
          deniedPatterns.add(UriPattern.parse(patterns[i].trim()));
        }
      }
    }
  }
  
  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    logger().info("Initializing the security module");
    Hub.getModules().getServerRuntime().addInterceptor(ServerPreInvokeEvent.class, this);
  }
  
  /**
   * @see Service#start()
   */
  public void start() throws Exception {
    logger().info("Starting the security module");
    isRunning = true;
  }
  
  /**
   * @see Service#dispose()
   */
  public void dispose() {
    logger().info("Stopping the security module");
    isRunning = false;
  }
  
  /**
   * 
   * @param evt a {@link ServerPreInvokeEvent}
   */
  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt) {
    if (!isRunning) {
      throw new IllegalStateException("This security module is currently not running");
    }
    
    TCPAddress addr = (TCPAddress) evt.getInvokeCommand().getConnection().getServerAddress();
    if (!isMatch(addr.getHost())) {
      logger().error("Security breach; could not execute: " + evt.getInvokeCommand().getMethodName());      
      throw new CorusSecurityException("Host does not have access to corus server: " + addr);
    }
  }
  
  protected synchronized boolean isMatch(String hostAddr) {
    if (allowedPatterns.size() == 0 && deniedPatterns.size() == 0) {
      return true;
    }

    boolean isMatching = (allowedPatterns.size() == 0);
    for (Iterator<UriPattern> it = allowedPatterns.iterator(); !isMatching && it.hasNext(); ) {
      UriPattern pattern = (UriPattern) it.next();
      if (pattern.matches(hostAddr)) {
        isMatching = true;
      }
    }
    
    for (Iterator<UriPattern> it = deniedPatterns.iterator(); isMatching && it.hasNext(); ) {
      UriPattern pattern = (UriPattern) it.next();
      if (pattern.matches(hostAddr)) {
        isMatching = false;
      }
    }
    return isMatching;
  }  
}
