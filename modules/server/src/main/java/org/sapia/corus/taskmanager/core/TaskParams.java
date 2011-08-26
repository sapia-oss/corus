package org.sapia.corus.taskmanager.core;

/**
 * This class is meant as a convenience for passing in multiple parameters to a {@link Task}.
 * 
 * @author yduchesne
 *
 * @param <P1> the generic type of the first param.
 * @param <P2> the generic type of the second param.
 * @param <P3> the generic type of the third param.
 * @param <P4> the generic type of the fourth param.
 * 
 * @see Task#execute(TaskExecutionContext, Object)
 */
public class TaskParams<P1,P2,P3,P4> {
  
  private P1 param1;
  private P2 param2;
  private P3 param3;
  private P4 param4;

  public P1 getParam1() {
    return param1;
  }
  
  public P2 getParam2() {
    return param2;
  }
  
  public P3 getParam3() {
    return param3;
  }
  
  public P4 getParam4() {
    return param4;
  }
  
  
  public static <P1, P2> TaskParams<P1, P2, Void, Void> createFor(P1 p1, P2 p2){
      TaskParams<P1, P2, Void, Void> params = new TaskParams<P1, P2, Void, Void>();
      params.param1 = p1;
      params.param2 = p2;
      return params;
  }
  
  public static <P1, P2, P3> TaskParams<P1, P2, P3, Void> createFor(P1 p1, P2 p2, P3 p3){
    TaskParams<P1, P2, P3, Void> params = new TaskParams<P1, P2, P3, Void>();
    params.param1 = p1;
    params.param2 = p2;
    params.param3 = p3;
    return params;
  }

  public static <P1, P2, P3, P4> TaskParams<P1, P2, P3, P4> createFor(P1 p1, P2 p2, P3 p3, P4 p4){
    TaskParams<P1, P2, P3, P4> params = new TaskParams<P1, P2, P3, P4>();
    params.param1 = p1;
    params.param2 = p2;
    params.param3 = p3;
    params.param4 = p4;
    return params;
  }
}
