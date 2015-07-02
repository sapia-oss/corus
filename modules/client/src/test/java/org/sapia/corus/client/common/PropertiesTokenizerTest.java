package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class PropertiesTokenizerTest {

  private String simpleLiteral                                = "n=v";
  private String literalWithEqualsSignInvalue                 = "n=v0=v1";
  private String literalWithEscapedEqualsSignInvalue          = "n=v0\\=v1";

  private String complexLiteral                               = "n0=v0,n1=v1,n2=v2";
  private String complexLiteralWithEscape                     = "n0=v0,n1=v1-0\\,v1-1,n2=v2";
  private String complexLiteralWithCommaInValueInLastProperty = "n0=v0,n1=v1,n2=v2-0,v2-1";

  @Test
  public void testSimpleLiteral() {
    PropertiesTokenizer tk = new PropertiesTokenizer(simpleLiteral);
    assertTrue(tk.hasNext());
    PairTuple<String, String> p = tk.next();

    assertEquals("n", p.getLeft());
    assertEquals("v", p.getRight());
  }
  
  @Test
  public void testLiteralWithEqualsSignInvalue() {
    PropertiesTokenizer tk = new PropertiesTokenizer(literalWithEqualsSignInvalue);
    assertTrue(tk.hasNext());
    PairTuple<String, String> p = tk.next();

    assertEquals("n", p.getLeft());
    assertEquals("v0=v1", p.getRight());
  }
  
  @Test
  public void testLiteralWithEscapedEqualsSignInvalue() {
    PropertiesTokenizer tk = new PropertiesTokenizer(literalWithEscapedEqualsSignInvalue);
    assertTrue(tk.hasNext());
    PairTuple<String, String> p = tk.next();

    assertEquals("n", p.getLeft());
    assertEquals("v0=v1", p.getRight());
  }

  @Test
  public void testComplexLiteral() {
    PropertiesTokenizer tk = new PropertiesTokenizer(complexLiteral);
    for (int i = 0; i < 3; i++) {
      assertTrue(tk.hasNext());
      PairTuple<String, String> p = tk.next();
      assertEquals("n" + i, p.getLeft());
      assertEquals("v" + i, p.getRight());
    }
  }

  @Test
  public void testComplexLiteralWithEscape() {
    PropertiesTokenizer tk = new PropertiesTokenizer(complexLiteralWithEscape);
    for (int i = 0; i < 3; i++) {
      assertTrue(tk.hasNext());
      PairTuple<String, String> p = tk.next();
      if (i == 1) {
        assertEquals("n" + i, p.getLeft());
        assertEquals("v1-0,v1-1", p.getRight());
      } else {
        assertEquals("n" + i, p.getLeft());
        assertEquals("v" + i, p.getRight());
      }
    }
  }
  
  @Test
  public void testComplexLiteralWithCommaInValueInLastProperty() {
    PropertiesTokenizer tk = new PropertiesTokenizer(complexLiteralWithCommaInValueInLastProperty);
    for (int i = 0; i < 3; i++) {
      assertTrue(tk.hasNext());
      PairTuple<String, String> p = tk.next();
      if (i == 2) {
        assertEquals("n" + i, p.getLeft());
        assertEquals("v2-0,v2-1", p.getRight());
      } else {
        assertEquals("n" + i, p.getLeft());
        assertEquals("v" + i, p.getRight());
      }
    }
  }
  
  @Test
  public void testAsProperties() {
    PropertiesTokenizer tk = new PropertiesTokenizer(complexLiteralWithCommaInValueInLastProperty);
    Properties props = tk.asProperties();
    for (int i = 0; i < 3; i++) {
      String name = "n" + i;
      if (i == 2) {
        assertEquals("v2-0,v2-1", props.getProperty(name));
      } else {
        assertEquals("v" + i, props.getProperty(name));
      }
    }
  }
}
