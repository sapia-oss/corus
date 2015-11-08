package org.sapia.corus.cloud.platform.workflow;

import java.util.Set;

/**
 * {@link WorkflowStep}s that also implement this interface have their execution performed in the context of
 * an error emanating from a previous step, provided any previous step for which the execution occured consists
 * of a prerequisite of this instance, as specified by the {@link #getGuardedExecutionPrerequisites()} method.
 * 
 * @author yduchesne
 */
public interface GuardedExecutionCapable {

  /**
   * @return the {@link Set} of {@link Class}es of the processing steps that must have
   * been executed prior to this instance.
   */
  public Set<Class<?>> getGuardedExecutionPrerequisites();
}
