package org.sapia.corus.client.common;

/**
 * Implemented by classes whose instances can test themselves for matching against
 * given {@link Pattern}s.
 * 
 * @author yduchesne
 *
 */
public interface Matcheable {

  /**
   * Abstracts the actual pattern implementation that's used.
   * 
   * @author yduchesne
   *
   */
  public interface Pattern {
   
    /**
     * @param value a {@link String} to test for matching.
     * @return <code>true</code> if the given string matches this pattern.
     */
    public boolean matches(String value);
    
  }
  
  /**
   * A default pattern implementation, wrapping an {@link Arg}.
   * 
   * @author yduchesne
   *
   */
  public static final class DefaultPattern implements Pattern {
    
    private Arg arg;
    
    public DefaultPattern(Arg arg) {
      this.arg = arg;
    }
    
    @Override
    public boolean matches(String value) {
      return arg.matches(value);
    }
    
    /**
     * @param patternString a pattern string.
     * @return a new {@link Pattern}.
     */
    public static Pattern parse(String patternString) {
      return new DefaultPattern(ArgFactory.parse(patternString));
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * A {@link Pattern} whose instances always return <code>true</code>.
   * 
   * @author yduchesne
   *
   */
  public static final class AnyPattern implements Pattern {
    
    @Override
    public boolean matches(String value) {
      return true;
    }
    
    /**
     * @return a new instance of this class.
     */
    public static Pattern newInstance() {
      return new AnyPattern();
    }
  }
  
  // ==========================================================================
  
  /**
   * @param pattern a {@link Pattern} to match.
   * @return <code>true</code> if this instance matches the given pattern.
   */
  public boolean matches(Pattern pattern);
}
