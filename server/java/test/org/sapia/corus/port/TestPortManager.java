/*
 * TestPortManager.java
 *
 * Created on October 18, 2005, 12:52 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.port;

import org.sapia.corus.admin.services.port.PortRange;
import org.sapia.corus.db.HashDbMap;

/**
 *
 * @author yduchesne
 */
public class TestPortManager extends PortManagerImpl{
  
  public TestPortManager() {
    super(new PortRangeStore(new HashDbMap<String, PortRange>()));
  }

}
