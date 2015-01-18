package org.sapia.corus.client.common.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A URL template, used to specify RESTful URLs and extract their values.
 * 
 * @author yduchesne
 *
 */
public class PathTemplate implements Comparable<PathTemplate> {
  
  private static final int PRIME = 31;
  
  /**
   * Holds the result of a {@link PathTemplate#matches(String)} invocation.
   * 
   * @author yduchesne
   */
  public static class MatchResult {
    
    private boolean matched;
    
    private Map<String, String> params;
    
    public MatchResult(boolean matched, Map<String, String> params) {
      this.matched = matched;
      this.params  = params;
    }
    
    /**
     * @return the path values that were matched.
     */
    public Map<String, String> getValues() {
      return params;
    }
    
    /**
     * @return <code>true</code> if there was a match, <code>false</code> otherwise.
     */
    public boolean matched() {
      return matched;
    }
    
  }
  
  // --------------------------------------------------------------------------

  private interface URLTemplatePart extends Comparable<URLTemplatePart> {
    
    public String getValue();
    
    public boolean matches(String value, Map<String, String> params);
    
  }
  
  // --------------------------------------------------------------------------
  
  private static class PathSegment implements URLTemplatePart {
    
    private String value;
   
    private PathSegment(String value) {
      this.value = value;
    }
    
    @Override
    public String getValue() {
      return value;
    }
    
    @Override
    public boolean matches(String value, Map<String, String> params) {
      return this.value.equals(value);
    }
    
    @Override
    public int compareTo(URLTemplatePart other) {
      return value.compareTo(other.getValue());
    }
    
    @Override
    public String toString() {
      return value;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private static class Variable implements URLTemplatePart {
    
    private String name;
    
    private Variable(String value) {
      this.name = value;
    }
    
    @Override
    public String getValue() {
      return name;
    }
    
    @Override
    public boolean matches(String value, Map<String, String> params) {
      params.put(name, value);
      return true;
    }
    
    @Override
    public int compareTo(URLTemplatePart other) {
      if (other instanceof Variable) {
        return name.compareTo(((Variable) other).name);
      } else {
        return 1;
      }
    }
    
    @Override
    public String toString() {
      return "{" + name + "}";
    }
  }
  
  // ==========================================================================
  
  private List<URLTemplatePart> parts;
  
  private PathTemplate(List<URLTemplatePart> parts) {
    this.parts = parts;
  }
  
  /**
   * @param url a URL string to match.
   * @return a {@link MatchResult}, resulting from the match test.
   */
  public MatchResult matches(String url) {
    String[] partValues = StringUtils.split(url, "/");
    return matches(partValues);
  }
  
  /**
   * @param partValues an array strings, with each string corresponding to a part in a path.
   * @return a {@link MatchResult}, resulting from the match test.
   */
  public MatchResult matches(String[] partValues) {
    Map<String, String> params = new HashMap<String, String>();
    if (partValues.length == parts.size()) {
      for (int i = 0; i < partValues.length; i++) {
        if (!parts.get(i).matches(partValues[i], params)) {
          return new MatchResult(false, params);
        }
      }
      return new MatchResult(true, params);
    }
    return new MatchResult(false, params);    
  }
                                                 
  
  /**
   * @param literal the representation of a URL path, in literal form.
   * @return
   */
  public static PathTemplate parse(String literal) {
    String[] partValues = StringUtils.split(literal, "/");
    List<URLTemplatePart> parts = new ArrayList<PathTemplate.URLTemplatePart>();
    for (String pv : partValues) {
      if (pv.startsWith("{") && pv.endsWith("}")) {
        parts.add(new Variable(pv.substring(pv.indexOf("{") + 1, pv.lastIndexOf("}"))));
      } else {
        parts.add(new PathSegment(pv));
      }
    }
    return new PathTemplate(parts);
  }
  
  @Override
  public int compareTo(PathTemplate other) {
    if (parts.size() == other.parts.size()) {
      int c = 0;
      for (int i = 0; i < parts.size(); i++) {
        URLTemplatePart part = parts.get(i);
        URLTemplatePart otherPart = other.parts.get(i);
        c = part.compareTo(otherPart);
        if (c != 0) {
          break;
        } 
      }
      return -c;
    }
    return - (parts.size() - other.parts.size());
  }
  
  @Override
  public int hashCode() {
    int h = 0;
    for (URLTemplatePart p : parts) {
      h = h + p.hashCode() * PRIME;
    }
    return h;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof PathTemplate) {
      PathTemplate other = (PathTemplate) o; 
      return this.compareTo(other) == 0;
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.size(); i++) {
      sb.append("/").append(parts.get(i));
    }
    return sb.toString();
  }
  
}
