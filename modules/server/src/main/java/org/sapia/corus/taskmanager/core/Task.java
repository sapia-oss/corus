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

  /**
   * Creates a new instance of this class.
   */
  public Task() {
    this.name = getClass().getSimpleName();
  }
  
  /**
   * Creates a new instance of this class.
   *
   * @param name the name of the task.
   */
  public Task(String name) {
    this.name = name;
  }
  
  /**
   * @return this instance's name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @param maxExecution the maximum number of times that this task
   * should be executed. 
   * 
   * @see #isMaxExecutionReached()
   */
  public void setMaxExecution(int maxExecution) {
    this.maxExecution = maxExecution;
  }

  /**
   * Executes this task.
   * 
   * @param ctx a {@link TaskExecutionContext}
   * @return an arbitrary object resulting from the execution.
   * @throws Throwable if an error occurs during execution.
   */
  public abstract Object execute(TaskExecutionContext ctx) throws Throwable;

  /**
   * @return <code>true</code> if this task has no parent.
   * @see #getParent()
   */
  public boolean isRoot(){
    return parent == null;
  }
  
  /**
   * @return <code>true</code> if this instance is a child task.
   * 
   * @see #addChild(Task)
   */
  public boolean isChild(){
    return parent != null;
  }

  /**
   * @return <code>true</code> if the maximum number of executions has been reached.
   */
  boolean isMaxExecutionReached(){
    return maxExecution > 0 && executionCount >= maxExecution;
  }

  /**
   * @return <code>true</code> if this task has been flagged as aborted.
   * 
   * @see #abort()
   */
  boolean isAborted() {
    return aborted;
  }
  
  /**
   * Flags this task as aborted.
   */
  protected synchronized void abort(){
    aborted = true;
  }
  
  /**
   * Flags this task as aborted.
   * 
   * @param ctx a {@link TaskExecutionContext}
   */
  protected synchronized void abort(TaskExecutionContext ctx){
    abort();
  }
  
  /**
   * Method that is internally called when the maximum number of executions
   * has been reached, if any maximum is specified.
   * 
   * @param ctx the {@link TaskExecutionContext}
   * @throws Throwable
   * @see #setMaxExecution(int)
   */
  protected void onMaxExecutionReached(TaskExecutionContext ctx) throws Throwable{}

  /**
   * @return the number of times this task has been executed thus far.
   */
  protected int getExecutionCount() {
    return executionCount;
  }
  
  /**
   * @return the maximum number of times to execute this task.
   */
  protected int getMaxExecution() {
    return maxExecution;
  }
  
  /**
   * Increments this instance's execution count.
   */
  void incrementExecutionCount(){
    if(executionCount == Integer.MAX_VALUE){
      executionCount = 0;
    }
    executionCount++;
  }
  
  /**
   * @return this instance's parent {@link Task}
   */
  Task getParent() {
    return parent;
  }
  
  /**
   * @param child a {@link Task} to add to this instance.
   */
  void addChild(Task child){
    if(child != this){
      child.parent = this;
      children.add(child);
    }
  }
  
  void cleanup(TaskExecutionContext ctx){

    if(isRoot() && children.size() == 0){
      ctx.close();
    }
    else if(isChild()){
      parent.children.remove(this);
    }
  }

  @Override
  public String toString() {
    return "["+getName()+"]";
  }

}
