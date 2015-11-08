package org.sapia.corus.deployer.artifact;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactManagerImplTest {
  
  @Mock
  private ArtifactProvider provider;
  
  private ArtifactManagerImpl manager;
  
  @Before
  public void setUp() {
    provider = mock(ArtifactProvider.class);
    manager = new ArtifactManagerImpl();
    
    manager.setProviders(Collects.arrayToList(provider));

    URI uri = URI.create("https://test");
    when(provider.accepts(uri)).thenReturn(true);
  }

  @Test
  public void testSelectProvider() {
    URI uri = URI.create("https://test");
    manager.selectProvider(uri);
  }

  @Test(expected = IllegalStateException.class)
  public void testSelectProvider_failure() {
    URI uri = URI.create("https://failed");
    manager.selectProvider(uri);
  }
}
