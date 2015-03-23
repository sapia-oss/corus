package org.sapia.corus.client.services.processor;

import static org.junit.Assert.*;

import org.junit.Test;

public class PortCriteriaTest {

  @Test
  public void testFromLiteral_matches_exact() {
    PortCriteria c = PortCriteria.fromLiteral("test:8000");
    assertTrue(c.getRange().matches("test"));
    assertTrue(c.getPort().matches("8000"));
  }

  @Test
  public void testFromLiteral_matches_any() {
    PortCriteria c = PortCriteria.fromLiteral("*:*");
    assertTrue(c.getRange().matches("test"));
    assertTrue(c.getPort().matches("8000"));
  }

  @Test
  public void testFromLiteral_matches_any_single_part() {
    PortCriteria c = PortCriteria.fromLiteral("*");
    assertTrue(c.getRange().matches("test"));
    assertTrue(c.getPort().matches("8000"));
  }
  
  @Test
  public void testFromLiteral_matches_any_emptyString() {
    PortCriteria c = PortCriteria.fromLiteral("");
    assertTrue(c.getRange().matches("test"));
    assertTrue(c.getPort().matches("8000"));
  }

}
