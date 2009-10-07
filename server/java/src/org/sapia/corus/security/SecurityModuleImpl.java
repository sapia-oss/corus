package org.sapia.corus.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.util.Property;
import org.sapia.soto.util.matcher.PathPattern;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
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
  
  private List _allowedPatterns = new ArrayList();
  private List _deniedPatterns = new ArrayList();
  
  private boolean _isRunning = false;
  
  /**
   * @see org.sapia.corus.admin.Module#getRoleName()
   */
  public String getRoleName() {
    return ROLE;
  }
  
  public boolean isRunning() {
    return _isRunning;
  }
  
  /**
   * Set the pattern list of the allowed hosts that can connect to
   * this corus server.
   * 
   * @param patternList The pattern list of allowed hosts.
   */  
  public synchronized void setAllowedHostPatterns(Property patternList) {
    _allowedPatterns.clear();
    
    if (patternList != null && patternList.getValue().trim().length() > 0) {
      String[] patterns = StringUtils.split(patternList.getValue(), ',');
      for (int i = 0; i < patterns.length; i++) {
        if (patterns[i].trim().equals("localhost")) {
          _allowedPatterns.add(PathPattern.parse(LOCALHOST, '.', false));
        } else {
          _allowedPatterns.add(PathPattern.parse(patterns[i].trim(), '.', false));
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
  public synchronized void setDeniedHostPatterns(Property patternList) {
    _deniedPatterns.clear();
    
    if (patternList != null && patternList.getValue().trim().length() > 0) {
      String[] patterns = StringUtils.split(patternList.getValue(), ',');
      for (int i = 0; i < patterns.length; i++) {
        if (patterns[i].trim().equals("localhost")) {
          _deniedPatterns.add(PathPattern.parse(LOCALHOST, '.', false));
        } else {
          _deniedPatterns.add(PathPattern.parse(patterns[i].trim(), '.', false));
        }
      }
    }
  }
  
  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    logger().info("Initializing the security module");
    Hub.serverRuntime.addInterceptor(ServerPreInvokeEvent.class, this);
  }
  
  /**
   * @see org.sapia.corus.ModuleHelper#start()
   */
  public void start() throws Exception {
    logger().info("Starting the security module");
    _isRunning = true;
  }
  
  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    logger().info("Stopping the security module");
    _isRunning = false;
  }
  
  /**
   * 
   * @param evt
   */
  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt) {
    if (!_isRunning) {
      throw new IllegalStateException("This security module is currently not running");
    }
    
    TCPAddress addr = (TCPAddress) evt.getInvokeCommand().getConnection().getServerAddress();
    if (!isMatch(addr.getHost())) {
      logger().error("Security breach; could not execute: " + evt.getInvokeCommand().getMethodName());      
      throw new CorusSecurityException("Host does not have access to corus server: " + addr);
    }
  }
  
  protected synchronized boolean isMatch(String hostAddr) {
    if (_allowedPatterns.size() == 0 && _deniedPatterns.size() == 0) {
      return true;
    }

    boolean isMatching = (_allowedPatterns.size() == 0);
    for (Iterator it = _allowedPatterns.iterator(); !isMatching && it.hasNext(); ) {
      PathPattern pattern = (PathPattern) it.next();
      if (pattern.matches(hostAddr)) {
        isMatching = true;
      }
    }
    
    for (Iterator it = _deniedPatterns.iterator(); isMatching && it.hasNext(); ) {
      PathPattern pattern = (PathPattern) it.next();
      if (pattern.matches(hostAddr)) {
        isMatching = false;
      }
    }
    return isMatching;
  }  
}
