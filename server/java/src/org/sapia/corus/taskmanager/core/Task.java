package org.sapia.corus.taskmanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is to be inherited by concrete tasks. 
 * 
 * @see TaskManager
 * 
 * @author yduchesne
 *
 */
public abstract class Task {

  private List<Task> children = Collections.synchronizedList(new ArrayList<Task>());
  private volatile boolean aborted;
  private volatile int executionCount, maxExecution;
  private String name;
  private Task parent;

  public Task() {
  }
  
  public Task(String name) {
    this.name = name;
  }
  
  public String getName() {
    if(name == null){ 
      name = getClass().getSimpleName(); 
    }
    return name;
  }
  
  public void setMaxExecution(int maxExecution) {
    this.maxExecution = maxExecution;
  }
  
  @Override
  public String toString() {
    return "["+getName()+"]";
  }
  
  boolean isAborted() {
    return aborted;
  }
  
  boolean isMaxExecutionReached(){
    return maxExecution > 0 && executionCount >= maxExecution;
  }
  
  public abstract Object execute(TaskExecutionContext ctx) throws Throwable;
  
  protected synchronized void abort(){
    aborted = true;
  }
  protected synchronized void abort(TaskExecutionContext ctx){
    abort();
  }
  
  protected void onMaxExecutionReached(TaskExecutionContext ctx) throws Throwable{}

  protected int getExecutionCount() {
    return executionCount;
  }
  
  protected int getMaxExecution() {
    return maxExecution;
  }
  
  void incrementExecutionCount(){
    if(executionCount == Integer.MAX_VALUE){
      executionCount = 0;
    }
    executionCount++;
  }
  
  Task getParent() {
    return parent;
  }
  
  public boolean isRoot(){
    return parent == null;
  }
  
  public boolean isChild(){
    return parent != null;
  }
  
  void addChild(Task child){
    if(child != this){
      child.parent = this;
      children.add(child);
    }
  }
  
  void cleanup(TaskExecutionContext ctx){

    if(isRoot() && children.size() == 0 && ctx.getLog() != null){
      ctx.getLog().close();
    }
    else if(isChild()){
      parent.children.remove(this);
    }
  }
  
}
