package org.sapia.corus.util;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;

@RunWith(MockitoJUnitRunner.class)
public class DynamicPropertyTest  {
  
  private DynamicProperty<String> prop;

  @Before
  public void setUp() {
    prop = new DynamicProperty<String>(String.class);
  }
  
  @Test
  public void testAddListener() {
    DynamicPropertyListener<String> listener = Mockito.mock(DynamicPropertyListener.class);
    prop.addListener(listener);
    
    prop.setValue("test");
 
    verify(listener).onModified(prop);
  }
  
  
  @Test
  public void testRemoveListener() {
    DynamicPropertyListener<String> listener = Mockito.mock(DynamicPropertyListener.class);
    prop.addListener(listener);
    
    prop.removeListener(listener);
    
    prop.setValue("test");
 
    verify(listener, never()).onModified(prop);
  }

}
