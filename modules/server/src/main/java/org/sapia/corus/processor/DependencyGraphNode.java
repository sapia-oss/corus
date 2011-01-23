package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyGraphNode {

  private List<DependencyGraphNode> children = new ArrayList<DependencyGraphNode>();
  private Set<ProcessRef> childRefs = new HashSet<ProcessRef>();
  
  private ProcessRef ref;
  
  public DependencyGraphNode() {
  }

  public DependencyGraphNode(ProcessRef ref) {
    this.ref = ref;
  }
  
  public boolean isRoot(){
    return ref == null;
  }
  
  public ProcessRef getProcessRef() {
    return ref;
  }
  
  public boolean add(ProcessRef ref){
    if(childRefs.add(ref)){
      children.add(new DependencyGraphNode(ref));
      return true;
    }
    else{
      return false;
    }
  }
  
  public boolean add(DependencyGraphNode child){
    if(childRefs.add(child.ref)){
      children.add(child);
      return true;
    }
    else{
      return false;
    }
  }
  
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
