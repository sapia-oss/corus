package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Corresponds to a node in the process dependency graph.
 * @author yduchesne
 *
 */
public class DependencyGraphNode {

  private List<DependencyGraphNode> children = new ArrayList<DependencyGraphNode>();
  private Set<ProcessRef> 					childRefs = new HashSet<ProcessRef>();
  private ProcessRef 								ref;
  
  public DependencyGraphNode() {
  }

  public DependencyGraphNode(ProcessRef ref) {
    this.ref = ref;
  }
  
  /**
   * @return <code>true</code> if this instance is thre root of the graph.
   */
  public boolean isRoot(){
    return ref == null;
  }
  
  /**
   * @return this instance's {@link ProcessRef}.
   */
  public ProcessRef getProcessRef() {
    return ref;
  }
  
  /**
   * Internally wraps the given {@link ProcessRef} in a new {@link DependencyGraphNode}
   * and adds it to this instance's children.
   * 
   * @param ref a {@link ProcessRef}.
   * @return <code>true</code> if the given process ref was added, <code>false</code> otherwise.
   */
  public boolean add(ProcessRef ref){
    if(childRefs.add(ref)){
      children.add(new DependencyGraphNode(ref));
      return true;
    } else{
      return false;
    }
  }
  
  /**
   * @param child a {@link DependencyGraphNode} to add as a child to this instance.
   * @return <code>true</code> if the child was added, <code>false</code> otherwise.
   */
  public boolean add(DependencyGraphNode child){
    if(childRefs.add(child.ref)){
      children.add(child);
      return true;
    } else{
      return false;
    }
  }
  
  /**
   * Returns this instance's graph as a flattened collection of {@link ProcessRef}, from
   * the most dependent to the less dependent.
   * 
   * @return this instance's graph, as a {@link Collection} of {@link ProcessRef}.
   */
  public Collection<ProcessRef> flatten(){
    List<ProcessRef> toReturn = new ArrayList<ProcessRef>();
    flatten(toReturn);
    return toReturn;
  }
  
  private void flatten(List<ProcessRef> toReturn){
    for(DependencyGraphNode child:children){
      if(!toReturn.contains(child)){
        child.flatten(toReturn);
      }
    }
    if(ref != null){
      if(!toReturn.contains(ref)){
        toReturn.add(ref);
      }
    }
  }
  
}
