package org.sapia.corus.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ResultTest {

  @Mock
  private ServerAddress                          addr;
  private CorusHost                              host;
  private Result<MatcheableString>               elementResult;
  private Result<List<MatcheableString>>         collectionResult;
  private Result<Map<Integer, MatcheableString>> mapResult;
  private Result<MatcheableString[]>             arrayResult;
 
  @Before
  public void setUp() {
    host             = CorusHost.newInstance(new Endpoint(addr, addr), "osInfo", "jvmInfo", mock(PublicKey.class));
    elementResult    = new Result<MatcheableString>(host, string("test"), Result.Type.ELEMENT);
    collectionResult = new Result<List<MatcheableString>>(host, Collects.arrayToList(string("1"), string("2"), string("3")), Result.Type.COLLECTION);
    Map<Integer, MatcheableString> map = new HashMap<Integer, MatcheableString>();
    map.put(1, string("1"));
    map.put(2, string("2"));
    map.put(3, string("3"));
    mapResult        = new Result<Map<Integer, MatcheableString>>(host, map, Result.Type.COLLECTION);
    arrayResult      = new Result<MatcheableString[]>(host, new MatcheableString[] {string("1"), string("2"), string("3")}, Result.Type.COLLECTION);
  }
  
  @Test
  public void testGetOrigin() {
    assertEquals(host, elementResult.getOrigin());
  }

  @Test
  public void testFilter_element() {
    Result<MatcheableString> filtered = elementResult.filter(Matcheable.DefaultPattern.parse("test"));
    assertEquals(1, filtered.size());
    assertEquals(string("test"), filtered.getData());
  }
  
  @Test
  public void testFilter_collection() {
    Result<List<MatcheableString>> filtered = collectionResult.filter(Matcheable.DefaultPattern.parse("1"));
    assertEquals(1, filtered.size());
    assertEquals(string("1"), filtered.getData().get(0));
  }
  
  @Test
  public void testFilter_array() {
    Result<MatcheableString[]> filtered = arrayResult.filter(Matcheable.DefaultPattern.parse("1"));
    assertEquals(1, filtered.size());
    assertEquals(string("1"), filtered.getData()[0]);
  }
  
  @Test
  public void testFilter_map() {
    Result<Map<Integer, MatcheableString>> filtered = mapResult.filter(Matcheable.DefaultPattern.parse("1"));
    assertEquals(1, filtered.size());
    assertEquals(string("1"), filtered.getData().get(new Integer(1)));
  }
  
  private MatcheableString string(String value) {
    return new MatcheableString(value);
  }
  
  public class MatcheableString implements Matcheable {
    
    private String value;
    
    public MatcheableString(String value) {
      this.value = value;
    }
    
    public String getValue() {
      return value;
    }
    
    @Override
    public boolean matches(Pattern pattern) {
      return pattern.matches(value);
    }
    
    @Override
    public String toString() {
      return value;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MatcheableString) {
        return ((MatcheableString) obj).getValue().equals(value);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }
}
