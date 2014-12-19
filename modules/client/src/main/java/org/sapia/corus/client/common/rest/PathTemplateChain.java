package org.sapia.corus.client.common.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;

public class PathTemplateChain {
  
  /**
   * Aggregates {@link PathTemplate}s, internally sorting them. Builds a new {@link PathTemplateChain}
   * out of the provided {@link PathTemplate}s.
   * 
   * @author yduchesne
   *
   */
  public static class Builder {
    
    private List<PathTemplate> templates = new ArrayList<PathTemplate>();

    private Builder() {
    }
    
    /**
     * @param template a {@link PathTemplate} to add.
     * @return this instance.
     */
    public Builder add(String literal) {
      templates.add(PathTemplate.parse(literal));
      return this;
    }
    
    /**
     * @param template a {@link PathTemplate} to add.
     * @return this instance.
     */
    public Builder add(PathTemplate template) {
      templates.add(template);
      return this;
    }
    
    /**
     * @return a new instance of this class.
     */
    public PathTemplateChain build() {
      Collections.sort(templates);
      return new PathTemplateChain(templates);
    }
    
  }
  
  // ==========================================================================
  
  private List<PathTemplate> templates;
  
  private PathTemplateChain(List<PathTemplate> templates) {
    this.templates = templates;
  }

  /**
   * @param path a path to match.
   * @return the {@link MatchResult} resulting from the match test.
   */
  public MatchResult matches(String path) {
    String[] partValues = StringUtils.split(path, "/");
    for (PathTemplate t : templates) {
      MatchResult result = t.matches(partValues);
      if (result.matched()) {
        return result;
      }
    }
    return new MatchResult(false, new HashMap<String, String>());
  }
}
