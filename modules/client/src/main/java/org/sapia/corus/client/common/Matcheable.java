package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  
  // --------------------------------------------------------------------------
  
  /**
   * Encapsulates a list of patterns. Depending on the operation ( {@link MatchOp#ALL} or {@link MatchOp#ANY}),
   * all or any of the encapsulated patterns must evaluate to <code>true</code> for an instance of this
   * class to itself return <code>true</code>. 
   * 
   * @author yduchesne
   *
   */
  public static final class CompositePattern implements Pattern {
    
    public enum MatchOp {
      ALL,
      ANY;
    }
    
    private MatchOp       op        = MatchOp.ANY;
    private List<Pattern> patterns  = new ArrayList<Matcheable.Pattern>();
    
    /**
     * @param p a {@link Pattern} to add to this instance's list of {@link Pattern}.
     * @return this instance.
     */
    public CompositePattern add(Pattern...p) {
      patterns.addAll(Arrays.asList(p));
      return this;
    }
    
    /**
     * Flags {@link MatchOp#ANY} logic.
     * 
     * @return this instance.
     */
    public CompositePattern any() {
      op = MatchOp.ANY;
      return this;
    }

    /**
     * Flags {@link MatchOp#ALL} logic.
     * 
     * @return this instance.
     */
    public CompositePattern all() {
      op = MatchOp.ALL;
      return this;
    }
    
    @Override
    public boolean matches(String value) {
      int matchCount = 0;
      for (Pattern p : patterns) {
        if (p.matches(value)) {
          matchCount++;
          if (op == MatchOp.ANY) {
            break;
          }
        }
      }
      
      if (op == MatchOp.ALL) {
        return matchCount == patterns.size();
      } else {
        return matchCount > 0;
      }
    }
    
    /**
     * A constructor method, provided for convenience.
     * 
     * @return a new instance this class.
     */
    public static final CompositePattern newInstance() {
      return new CompositePattern();
    }
    
  }
  
  // ==========================================================================
  
  /**
   * @param pattern a {@link Pattern} to match.
   * @return <code>true</code> if this instance matches the given pattern.
   */
  public boolean matches(Pattern pattern);
}
