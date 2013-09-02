package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CollectionUtilsTest {

  @Test
  public void testEmptyIfNullList() {
    List<String> input = null;
    List<String> lst = CollectionUtils.emptyIfNull(input);
    assertNotNull("Expected non-null list", lst);
  }

  @Test
  public void testEmptyIfNullSet() {
    Set<String> input = null;
    Set<String> set = CollectionUtils.emptyIfNull(input);
    assertNotNull("Expected non-null set", set);
  }

}
