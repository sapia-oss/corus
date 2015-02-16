package org.sapia.corus.http.filesystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemExtensionTest {
  
  @Mock
  private ServerContext          context;
  
  @Mock
  private InternalServiceContext services;
  
  @Mock
  private EventDispatcher        dispatcher;
  
  @Mock
  private Configurator           configurator;
  
  @Mock
  private File                   file;
  
  private FileSystemExtension    ext;
  
  @Before
  public void setUp() {
    when(file.getAbsolutePath()).thenReturn("test");
    when(context.getCorusProperties()).thenReturn(new Properties());
    when(context.getServices()).thenReturn(services);
    when(services.getConfigurator()).thenReturn(configurator);
    when(configurator.getProperties(PropertyScope.SERVER, new ArrayList<String>(0))).thenReturn(new Properties());
    when(services.getEventDispatcher()).thenReturn(dispatcher);
    ext = new FileSystemExtension(context);
  }

  @Test
  public void testOnPropertyChangeEvent_hidden_file_pattern_added() {
    PropertyChangeEvent event = new PropertyChangeEvent(CorusConsts.PROPERTY_CORUS_FILE_HIDE_PATTERNS, "test", PropertyScope.SERVER, Type.ADD);
    ext.onPropertyChangeEvent(event);
    
    assertFalse(ext.isAccessAllowed(file));
  }
  
  @Test
  public void testOnPropertyChangeEvent_hidden_file_pattern_removed() {
    PropertyChangeEvent event = new PropertyChangeEvent(CorusConsts.PROPERTY_CORUS_FILE_HIDE_PATTERNS, "", PropertyScope.SERVER, Type.REMOVE);
    ext.onPropertyChangeEvent(event);
    
    assertTrue(ext.isAccessAllowed(file));
  }

}
