package org.sapia.corus.client.services.port;

import org.junit.Assert;
import org.junit.Test;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.NoSuchFieldException;

public class PortRangeTest {

  @Test
  public void testClassDescriptor() throws Exception {
    ClassDescriptor<PortRange> range = new ClassDescriptor<PortRange>(PortRange.class);
    try{
      range.getFieldForName("key");
      Assert.fail("Should not have been able to acquire transient field");
    }catch(NoSuchFieldException e){
      //ok
    }
    
    range.getFieldForName("min");
    range.getFieldForName("max");
    range.getFieldForName("available");
    range.getFieldForName("active");    
  }

}
