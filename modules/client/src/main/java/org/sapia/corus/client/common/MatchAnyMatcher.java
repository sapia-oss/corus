package org.sapia.corus.client.common;

/**
 * An instance of this class is used to match any string.
 * 
 * @author jcdesrochers
 */
public class MatchAnyMatcher implements ArgMatcher {

  public static final long serialVersionUID = 1L;

  @Override
  public boolean matches(String str) {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof MatchAnyMatcher);
  }

}
