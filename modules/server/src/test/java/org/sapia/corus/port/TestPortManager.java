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

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

/**
 *
 * @author yduchesne
 */
public class TestPortManager extends PortManagerImpl{
  
  public TestPortManager() {
    super(new PortRangeStore(new InMemoryDbMap<String, PortRange>(new ClassDescriptor<PortRange>(PortRange.class), new Func<PortRange, JsonInput>() {
      public PortRange call(JsonInput arg0) {
        throw new UnsupportedOperationException();
      }
    })));
  }

}
