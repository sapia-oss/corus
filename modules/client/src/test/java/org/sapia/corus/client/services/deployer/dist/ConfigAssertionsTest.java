package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeAtLeast;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeGreater;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNull;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.elementAtleast;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.elementExpectsInstanceOf;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.elementNotNull;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.elementNotNullOrEmpty;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.optionalAttributeNotNullOrEmpty;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Collects;
import org.sapia.util.xml.confix.ConfigurationException;

public class ConfigAssertionsTest {
  private static final String E = "test-element";
  private static final String A = "test-attribute";

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testAttributeNotNull_is_not_null() throws ConfigurationException {
    attributeNotNull(E, A, "test");
  }
  @Test(expected = ConfigurationException.class)
  public void testAttributeNotNull_is_null() throws ConfigurationException {
    attributeNotNull(E, A, null);
  }

  @Test
  public void testAttributeNotNullOrEmpty_is_not_null() throws ConfigurationException {
    attributeNotNull(E, A, "test");
  }
  
  @Test(expected = ConfigurationException.class)
  public void testAttributeNotNullOrEmpty_is_null() throws ConfigurationException {
    attributeNotNullOrEmpty(E, A, null);
  }
  
  @Test(expected = ConfigurationException.class)
  public void testAttributeNotNullOrEmpty_is_empty() throws ConfigurationException {
    attributeNotNullOrEmpty(E, A, "  ");
  }
  
  @Test
  public void testOptionalAttributeNotNullOrEmpty_is_set() throws ConfigurationException {
    optionalAttributeNotNullOrEmpty(E, A, OptionalValue.of("test"));
  }
  
  @Test
  public void testOptionalAttributeNotNullOrEmpty_is_null() throws ConfigurationException {
    OptionalValue<String> value = OptionalValue.none();
    optionalAttributeNotNullOrEmpty(E, A, value);
  }
  
  @Test(expected = ConfigurationException.class)
  public void testOptionalAttributeNotNullOrEmpty_is_empty() throws ConfigurationException {
    optionalAttributeNotNullOrEmpty(E, A, OptionalValue.of("  "));
  }

  @Test
  public void testAttributeGreater_is_greater() throws ConfigurationException {
    attributeGreater(E, A, 0, 1);
  }
  
  @Test(expected = ConfigurationException.class)
  public void testAttributeGreater_is_equal() throws ConfigurationException {
    attributeGreater(E, A, 0, 0);
  }
  
  @Test(expected = ConfigurationException.class)
  public void testAttributeGreater_is_smaller() throws ConfigurationException {
    attributeGreater(E, A, 0, -1);
  }

  @Test
  public void testAttributAtLeast_is_equal() throws ConfigurationException {
    attributeAtLeast(E, A, 0, 0);
  }
  
  @Test
  public void testAttributAtLeast_is_greater() throws ConfigurationException {
    attributeAtLeast(E, A, 0, 1);
  }
  
  @Test(expected = ConfigurationException.class)
  public void testAttributAtLeast_is_smaller() throws ConfigurationException {
    attributeAtLeast(E, A, 0, -1);
  }

  @Test
  public void testElementNotNull_is_not_null() throws ConfigurationException {
    elementNotNull(E, "test");
  }
  
  @Test(expected = ConfigurationException.class)
  public void testElementNotNull_is_null() throws ConfigurationException {
    elementNotNullOrEmpty(E, null);
  }
  
  @Test
  public void testElementNotNullOrEmpty_is_not_null() throws ConfigurationException {
    elementNotNullOrEmpty(E, "test");
  }
  
  @Test(expected = ConfigurationException.class)
  public void testElementNotNullOrEmpty_is_null() throws ConfigurationException {
    elementNotNullOrEmpty(E, null);
  }

  @Test(expected = ConfigurationException.class)
  public void testElementNotNullOrEmpty_is_empty() throws ConfigurationException {
    elementNotNullOrEmpty(E, "  ");
  }

  @Test
  public void testElementAtleast_size_equal() throws ConfigurationException {
    elementAtleast(E, 1, Collects.arrayToList(new Integer(1)));
  }
  
  @Test
  public void testElementAtleast_size_greater() throws ConfigurationException {
    elementAtleast(E, 0, Collects.arrayToList(new Integer(1)));
  }
  
  @Test(expected = ConfigurationException.class)
  public void testElementAtleast_size_smaller() throws ConfigurationException {
    elementAtleast(E, 2, Collects.arrayToList(new Integer(1)));
  }

  @Test
  public void testElementExpectsInstanceOf_valid_instance() throws ConfigurationException {
    elementExpectsInstanceOf(E, Date.class, new java.sql.Date(System.currentTimeMillis()));
  }

  @Test(expected = ConfigurationException.class)
  public void testElementExpectsInstanceOf_invalid_instance() throws ConfigurationException {
    elementExpectsInstanceOf(E, String.class, new Integer(0));
  }
}
