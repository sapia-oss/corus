package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.PartitionServiceImpl;
import org.sapia.corus.client.rest.PartitionService.Partition;
import org.sapia.corus.client.rest.PartitionService.PartitionSet;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;

@RunWith(MockitoJUnitRunner.class)
public class PartitionServiceImplTest {

  @Mock
  private CorusConnector       connector;
  
  @Mock
  private CorusConnectionContext context;
  
  @Mock
  private ConfiguratorFacade   facade;
  
  private TimeValue            timeout;
  
  private MutableClock         clock;
  
  private PartitionServiceImpl partitionService;
  
  @Before
  public void setUp() {
    timeout          = TimeValue.createSeconds(60);
    clock            = new MutableClock();
    partitionService = new PartitionServiceImpl();
    partitionService.setClock(clock);
        
    when(connector.getConfigFacade()).thenReturn(facade);
    when(connector.getContext()).thenReturn(context);
  }
  
  @Test
  public void testCreatePartitionSet_all_nodes() {
    setupConfigFacade(5);
    PartitionSet partitionSets = partitionService.createPartitionSet(2, new ArrayList<ArgMatcher>(), new ArrayList<ArgMatcher>(), connector, timeout);
    assertEquals(2, partitionSets.getPartitionSize());
    assertEquals(5, partitionSets.getPartitions().size());
    for (int i = 0; i < 5; i++) {
      Partition p = partitionSets.getPartition(i);
      assertEquals(2, p.getHosts().size());
    }
  }
  
  @Test
  public void testCreatePartitionSet_explicit_includes() {
    setupConfigFacade(5);
    PartitionSet partitionSets = partitionService.createPartitionSet(2, Collects.arrayToList(ArgMatchers.parse("f*")), new ArrayList<ArgMatcher>(), connector, timeout);
    assertEquals(2, partitionSets.getPartitionSize());
    assertEquals(3, partitionSets.getPartitions().size());
    for (Partition p : partitionSets.getPartitions()) {
      for (CorusHost h : p.getHosts()) {
        assertTrue(Integer.parseInt(h.getOsInfo()) < 5);
      }
    }
  }
  
  @Test
  public void testCreatePartitionSet_explicit_excludes() {
    setupConfigFacade(5);
    PartitionSet partitionSets = partitionService.createPartitionSet(2, new ArrayList<ArgMatcher>(), 
        Collects.arrayToList(ArgMatchers.parse("f*")), connector, timeout);
    assertEquals(2, partitionSets.getPartitionSize());
    assertEquals(3, partitionSets.getPartitions().size());
    for (Partition p : partitionSets.getPartitions()) {
      for (CorusHost h : p.getHosts()) {
        assertTrue(Integer.parseInt(h.getOsInfo()) > 4);
      }
    }
  }
  
  @Test
  public void testCreatePartitionSet_explicit_includes_excludes() {
    setupConfigFacade(5);
    PartitionSet partitionSets = partitionService.createPartitionSet(2, Collects.arrayToList(ArgMatchers.parse("foo1")), 
        Collects.arrayToList(ArgMatchers.parse("bar2")), connector, timeout);
    assertEquals(2, partitionSets.getPartitionSize());
    assertEquals(3, partitionSets.getPartitions().size());
    for (Partition p : partitionSets.getPartitions()) {
      for (CorusHost h : p.getHosts()) {
        assertTrue(Integer.parseInt(h.getOsInfo()) < 5);
      }
    }
  }
  
  @Test
  public void testCreatePartitionSet_multiple_explicit_includes_excludes() {
    setupConfigFacade(5);
    PartitionSet partitionSets = partitionService.createPartitionSet(2, Collects.arrayToList(ArgMatchers.parse("foo1"), 
        ArgMatchers.parse("foo2")), Collects.arrayToList(ArgMatchers.parse("bar2")), connector, timeout);
    assertEquals(2, partitionSets.getPartitionSize());
    assertEquals(3, partitionSets.getPartitions().size());
    for (Partition p : partitionSets.getPartitions()) {
      for (CorusHost h : p.getHosts()) {
        assertTrue(Integer.parseInt(h.getOsInfo()) < 5);
      }
    }
  }

  @Test
  public void testGetPartitionSet() {
    PartitionSet partitionSet = partitionService.createPartitionSet(2, new ArrayList<ArgMatcher>(), 
        new ArrayList<ArgMatcher>(), connector, timeout);
    partitionService.getPartitionSet(partitionSet.getId());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGetPartitionSet_not_found() {
    partitionService.getPartitionSet("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeletePartitionSet() {
    PartitionSet partitionSet = partitionService.createPartitionSet(2, new ArrayList<ArgMatcher>(), 
        new ArrayList<ArgMatcher>(), connector, timeout);
    partitionService.deletePartitionSet(partitionSet.getId());
    partitionService.getPartitionSet(partitionSet.getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFlushStalePartitionSets() {
    PartitionSet partitionSet = partitionService.createPartitionSet(2, new ArrayList<ArgMatcher>(), 
        new ArrayList<ArgMatcher>(), connector, timeout);
    clock.increaseCurrentTimeMillis(timeout.getValueInMillis() + 1);
    partitionService.flushStalePartitionSets();
    partitionService.getPartitionSet(partitionSet.getId());
  }

  private void setupConfigFacade(final int numHostsOfEachTag) {
    
    CorusHost             thisHost   = null;
    Collection<CorusHost> otherHosts = new ArrayList<CorusHost>();
    
    int count = 0;
    for (int i = 0; i < numHostsOfEachTag; ++i) {
      if (i == 0) {
        thisHost = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
        count++;
      } else {
        CorusHost host = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
        otherHosts.add(host);
        count++;
      }
    }
    for (int i = 0; i < numHostsOfEachTag; ++i) {
      if (thisHost == null) {
        thisHost = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
        count++;
      } else {
        CorusHost host = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
        otherHosts.add(host);
        count++;
      }
    }    
    
    when(context.getServerHost()).thenReturn(thisHost);
    when(context.getOtherHosts()).thenReturn(otherHosts);
    
    doAnswer(new Answer<Results<Set<Tag>>>() {
      int count = 0;
      
      @Override
      public Results<Set<Tag>> answer(InvocationOnMock invocation)
          throws Throwable {
        
        Results<Set<Tag>> tagResults = new Results<>();
        if (count == 0) {
          Set<Tag> tagSet = Collects.arrayToSet(new Tag("foo1"), new Tag("foo2"));
          CorusHost host  = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
          Result<Set<Tag>> tags = new Result<Set<Tag>>(host, tagSet, Result.Type.COLLECTION);
          tagResults.addResult(tags);
          count++;
        } else {
          for (int i = 0; i < numHostsOfEachTag - 1; i++) {
            Set<Tag> tagSet = Collects.arrayToSet(new Tag("foo1"), new Tag("foo2"));
            CorusHost host  = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
            Result<Set<Tag>> tags = new Result<Set<Tag>>(host, tagSet, Result.Type.COLLECTION);
            tagResults.addResult(tags);
            count++;
          }
          
          for (int i = 0; i < numHostsOfEachTag; i++) {
            Set<Tag> tagSet = Collects.arrayToSet(new Tag("bar1"), new Tag("bar2"));
            CorusHost host  = CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "" + count, "jvm-" + count, mock(PublicKey.class));
            Result<Set<Tag>> tags = new Result<Set<Tag>>(host, tagSet, Result.Type.COLLECTION);
            tagResults.addResult(tags);
            count++;
          }
        }
        return tagResults;
      }
    }).when(facade).getTags(any(ClusterInfo.class));
  }
  
  
}
