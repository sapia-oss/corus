package org.sapia.corus.client.common.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;
import org.sapia.corus.client.common.rest.PathTemplate.URLTemplatePart;
import org.sapia.ubik.util.Condition;

/**
 * A data structure that optimizes matching against {@link PathTemplate}s.
 * 
 * @author yduchesne
 *
 */
public class PathTemplateTree<T> {
  
  private static class TreeNode<T> {
    
    private TreeMap<URLTemplatePart, TreeNode<T>> nodes = new TreeMap<>();

    private List<T> values = new ArrayList<>(5);
    
    private TreeNode() {
    }
    
     private void bind(int partIndex, PathTemplate template, T value) {
      if (partIndex < template.getParts().size()) {
        TreeNode<T> node = nodes.get(template.getParts().get(partIndex));
        if (node == null) {
          node = new TreeNode<T>();
          nodes.put(template.getParts().get(partIndex), node);
        }
        if (partIndex == template.getParts().size() - 1) {
          node.values.add(value);
        }
        node.bind(partIndex + 1, template, value);
      }
    }
    
    private PairTuple<MatchResult, OptionalValue<T>> match(int index, Map<String, String> params, String[] segments, Condition<T> selector) {
      if (index == segments.length) {
        OptionalValue<T> toReturn = select(selector);
        if (toReturn.isNull()) {
          return new PairTuple<MatchResult, OptionalValue<T>>(new MatchResult(false, params), toReturn);
        } else {
          return new PairTuple<MatchResult, OptionalValue<T>>(new MatchResult(true, params), toReturn);
        }
      } else {
        for (URLTemplatePart p : nodes.keySet()) {
          if (p.matches(segments[index], params)) {
            TreeNode<T> n = nodes.get(p);
            return n.match(index + 1, params, segments, selector);
          }
        }
        OptionalValue<T> none = OptionalValue.none();
        return new PairTuple<MatchResult, OptionalValue<T>>(new MatchResult(false, params), none);
      }
    }
    
    private OptionalValue<T> select(Condition<T> selector) {
      for (T v : values) {
        if (selector.apply(v)) {
          return OptionalValue.of(v);
        }
      }
      OptionalValue<T> none = OptionalValue.none();
      return none;
    }
  }
  
  // ==========================================================================
  
  private Map<URLTemplatePart, TreeNode<T>> nodes = new TreeMap<>();
  
  /**
   * @param template a {@link PathTemplate} to add to this instance.
   * @param an arbitrary object to associate to the given template.
   * @return this instance.
   */
  public PathTemplateTree<T> addTemplate(PathTemplate template, T value) {
    if (!template.getParts().isEmpty()) {
      int index = 0;
      URLTemplatePart part = template.getParts().get(index);
      TreeNode<T>     node = nodes.get(part);
      if (node == null) {
        node = new TreeNode<>();
        nodes.put(part, node);
      }   
      if (index == template.getParts().size() - 1) {
        node.values.add(value);
      }
      node.bind(index + 1, template, value);
    }
    return this;
  }
  
  /**
   * @param path a path to match.
   * @param selector the selection condition to use.
   * @return the {@link MatchResult}, holding data corresponding to the outcome of this operation.
   */
  public PairTuple<MatchResult, OptionalValue<T>> matches(String path, Condition<T> selector) {
    String[]            segments = StringUtils.split(path, '/');
    Map<String, String> params   = new HashMap<>();
    if (segments.length > 0) {
      int index = 0;
      for (URLTemplatePart p : nodes.keySet()) {
        if (p.matches(segments[index], params)) {
          TreeNode<T> n = nodes.get(p);
          return n.match(index + 1, params, segments, selector);
        }
      }
      OptionalValue<T> none = OptionalValue.none();
      return new PairTuple<MatchResult, OptionalValue<T>>(new MatchResult(false, params), none);
    } else {
      OptionalValue<T> none = OptionalValue.none();
      return new PairTuple<MatchResult, OptionalValue<T>>(new MatchResult(false, params), none);
    }
  }
  
}
