package org.sapia.corus.tomcat.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A performant pattern matching implementation that works on top of the
 * <code>UriPatternHelper</code> class.
 * 
 * @author Yanick Duchesne
 * 
 * <dl>
 * <dt><b>Copyright: </b>
 * <dd>Copyright &#169; 2002-2011 <a href="http://www.sapia-oss.org">Sapia Open
 * Source Software </a>. All Rights Reserved.</dd>
 * </dt>
 * <dt><b>License: </b>
 * <dd>Read the license.txt file of the jar or visit the <a
 * href="http://www.sapia-oss.org/license.html">license page </a> at the Sapia
 * OSS web site</dd>
 * </dt>
 * </dl>
 */
public class UriPattern {

  private int[]  _compiled;

  private UriPattern(int[] compiled) {
    _compiled = compiled;
  }

  /**
   * @param pattern
   *          a string pattern
   * @return a <code>UriPattern</code> instance.
   */
  public static UriPattern parse(String pattern) {
    return new UriPattern(UriPatternHelper.compilePattern(pattern));

  }

  /**
   * @see org.sapia.soto.util.matcher.Pattern#matches(java.lang.String)
   */
  public boolean matches(String str) {
    return matchResult(str).matched;
  }

  /**
   * Tests if the given string matches the pattern represented by this instance.
   * 
   * @param data
   *          a string to test this pattern against.
   * @return a <code>MatchResult</code>.
   */
  public MatchResult matchResult(String data) {
    Map map = new HashMap();
    MatchResult res = new MatchResult();
    res.result = map;
    res.matched = UriPatternHelper.match(map, data, _compiled);
    return res;
  }

  /**
   * This class represents the result of a pattern-matching operation
   * 
   * @author Yanick Duchesne
   * 
   * <dl>
   * <dt><b>Copyright: </b>
   * <dd>Copyright &#169; 2002-2011¯ <a href="http://www.sapia-oss.org">Sapia
   * Open Source Software </a>. All Rights Reserved.</dd>
   * </dt>
   * <dt><b>License: </b>
   * <dd>Read the license.txt file of the jar or visit the <a
   * href="http://www.sapia-oss.org/license.html">license page </a> at the Sapia
   * OSS web site</dd>
   * </dt>
   * </dl>
   */
  public static final class MatchResult {

    /**
     * The <code>Map</code> containing the tokens that have been matched. The
     * token are bound under the index corresponding to the order in which they
     * have been matched.
     */
    public Map     result;

    /**
     * Returns <code>true</code> if the matching operation was successful.
     */
    public boolean matched;
  }

}
