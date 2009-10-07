package org.sapia.corus.security;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.sapia.corus.util.StringProperty;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SecurityModuleImplTest extends TestCase {
  
  public SecurityModuleImplTest(String name){
    super(name);
  }
  
  public void testNoPatterns() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    assertTrue(mod.isMatch("dummy"));
  }
  
  public void testAllowedMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setAllowedHostPatterns(new StringProperty("192.170.1.1, 192.168.**"));

    boolean isAllowed = mod.isMatch("192.168.0.1");
    assertTrue("The hosts 192.168.0.1 should match the allowed pattern", isAllowed);
  }  

  public void testAllowedNoMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setAllowedHostPatterns(new StringProperty("192.170.1.1, 192.168.**"));

    boolean isAllowed = mod.isMatch("200.168.0.1");
    assertTrue("The hosts 192.168.0.1 should not match the allowed pattern", !isAllowed);
  }
  
  public void testDeniedMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setDeniedHostPatterns(new StringProperty("192.170.1.1, 192.168.**"));

    boolean isAllowed = mod.isMatch("192.168.0.1");
    assertTrue("The hosts 192.168.0.1 should match the denied pattern", !isAllowed);
  }  
  
  public void testDeniedNoMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setDeniedHostPatterns(new StringProperty("192.170.1.1, 192.168.**"));

    boolean isAllowed = mod.isMatch("200.168.0.1");
    assertTrue("The hosts 200.168.0.1 should not match the denied pattern", isAllowed);
  }  

  public void testComplexAllowedMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setAllowedHostPatterns(new StringProperty("192.170.1.1, 192.168.**, 10.10.10.**"));
    mod.setDeniedHostPatterns(new StringProperty("200.170.1.1, 200.168.**, 10.10.10.10"));

    boolean isAllowed = mod.isMatch("192.168.0.1");
    assertTrue("The hosts 192.168.0.1 should match the allowed pattern", isAllowed);

    isAllowed = mod.isMatch("10.10.10.1");
    assertTrue("The hosts 10.10.10.1 should match the allowed pattern", isAllowed);
  }  

  public void testComplexDeniedMatch() {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setAllowedHostPatterns(new StringProperty("192.170.1.1, 192.168.**, 10.10.10.**"));
    mod.setDeniedHostPatterns(new StringProperty("200.170.1.1, 200.168.**, 10.10.10.10"));

    boolean isAllowed = mod.isMatch("200.168.0.1");
    assertTrue("The hosts 200.168.0.1 should match the denied pattern", !isAllowed);

    isAllowed = mod.isMatch("10.10.10.10");
    assertTrue("The hosts 10.10.10.10 should match the denied pattern", !isAllowed);
  }  

  public void testAllowedLocalhost() throws Exception {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setAllowedHostPatterns(new StringProperty("localhost"));
    String localhost = InetAddress.getLocalHost().getHostAddress();

    boolean isAllowed = mod.isMatch(localhost);
    assertTrue("The localhost [" + localhost + "] should match the allowed pattern", isAllowed);
  }  

  public void testDeniedLocalhost() throws Exception {
    SecurityModuleImpl mod = new SecurityModuleImpl();
    mod.setDeniedHostPatterns(new StringProperty("localhost"));
    String localhost = InetAddress.getLocalHost().getHostAddress();

    boolean isAllowed = mod.isMatch(localhost);
    assertTrue("The localhost [" + localhost + "] should match the denied pattern", !isAllowed);
  }  
}
