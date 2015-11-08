package org.sapia.corus.cloud.platform.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.cloud.platform.util.TimeMeasure;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A default {@link Workflow} implementations.
 * 
 * @author yduchesne
 *
 * @param <C> a generic specifying the type of {@link WorkflowContext} on which the implementation depends.
 */
public class WorkflowImpl<C extends WorkflowContext> implements Workflow<C>{
  
  private class StepWrapper {
    private StepDescriptor desc;
    private WorkflowStep<C> step;
  }
  
  // ==========================================================================

  private WorkflowLog              log;
  private List<StepWrapper>        wrappers;
  private List<WorkflowStepResult> results = new ArrayList<WorkflowStepResult>();
  private TimeMeasure              startTime;
  private boolean                  hasError;
  
  public WorkflowImpl(WorkflowLog log, List<WorkflowStep<C>> steps) {
    this.log = log;
    wrappers = Lists.transform(steps, new Function<WorkflowStep<C>, StepWrapper>() {
      @Override
      public StepWrapper apply(WorkflowStep<C> s) {
        StepWrapper w = new StepWrapper();
        w.desc = new StepDescriptor(decamelizeName(s.getClass().getSimpleName()), s.getDescription());
        w.step = s;
        return w;
      }
    });
    
    for (StepWrapper w : wrappers) {
      log.verbose("Preparing to executing the following steps:");
      log.verbose("  - %s", w.desc.getStepName());
    }
  }
  
  @Override
  public List<StepDescriptor> getStepDescriptors() {
    List<StepDescriptor> toReturn = new ArrayList<StepDescriptor>();
    Lists.transform(wrappers, new Function<StepWrapper, StepDescriptor>() {
      @Override
      public org.sapia.corus.cloud.platform.workflow.Workflow.StepDescriptor apply(StepWrapper input) {
        return input.desc;
      }
    });
    return toReturn;
  }
  
  @Override
  public WorkflowResult getResult() {
    Preconditions.checkState(startTime != null, "Workflow was not executed");
    return new WorkflowResult(hasError ? WorkflowResult.Outcome.FAILURE : WorkflowResult.Outcome.SUCCESS, results, startTime.elapsedMillis());
  }
  
  @Override
  public void execute(C context) {
    Preconditions.checkState(results.isEmpty(), "Workflow was already executed (cannot be executed more than once)");
    Set<Class<?>> runStepClasses = new HashSet<Class<?>>();
    log.info("Starting workflow");
    startTime = TimeMeasure.forCurrentTime(context.getTimeSupplier());
    
    for (StepWrapper w : wrappers) {
      if (hasError) {
        if (w.step instanceof GuardedExecutionCapable) {
          Set<Class<?>> preReqs = ((GuardedExecutionCapable) w.step).getGuardedExecutionPrerequisites();
          for (Class<?> p : preReqs) {
            if (hasPrerequisite(runStepClasses, p)) {
              TimeMeasure start = TimeMeasure.forCurrentTime(context.getTimeSupplier());
              try {
                log.info("==> Step: %s (%s)", w.desc.getStepName(), w.desc.getStepDescription());
                log.info("%s step completed (took %s)", w.desc.getStepName(), 
                    start.elapsedMillis().approximate().toLiteral());
                w.step.execute(context);
                results.add(new WorkflowStepResult(w.desc, null, Phase.NORMAL, start.elapsedMillis()));
                runStepClasses.add(w.step.getClass());
              } catch (Exception e) {
                results.add(new WorkflowStepResult(w.desc, e, Phase.CLEANUP, start.elapsedMillis()));
                log.warning("Error caught running step %s: %s", w.desc.getStepName(), e.getMessage());
                log.verbose(e);
              }
              break;
            }
          }
        }
      } else {
        TimeMeasure start = TimeMeasure.forCurrentTime(context.getTimeSupplier());
        try {
          log.info("==> Step: %s (%s)", w.desc.getStepName(), w.desc.getStepDescription());
          w.step.execute(context);
          log.info("%s step completed (took %s)", w.desc.getStepName(), 
              start.elapsedMillis().approximate().toLiteral());
          results.add(new WorkflowStepResult(w.desc, null, Phase.NORMAL, start.elapsedMillis()));
          runStepClasses.add(w.step.getClass());
        } catch (Exception e) {
          results.add(new WorkflowStepResult(w.desc, e, Phase.CLEANUP, start.elapsedMillis()));
          log.warning("Error caught running step %s: %s", w.desc.getStepName(), e.getMessage());
          log.verbose(e);
          log.warning("Normal worflow will be bypassed, proceeding to run cleanup steps");
          hasError = true;
        }
      }
    }
    
    log.info("Completed workflow");
  }
  
  private boolean hasPrerequisite(Set<Class<?>> runStepClasses, Class<?> preReq) {
    for (Class<?> r : runStepClasses) {
      if (preReq.isAssignableFrom(r)) {
        return true;
      }
    }
    return false;
  }
  
  static String decamelizeName(String name) {
    StringBuilder newName = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i == 0) {
          newName.append(Character.toLowerCase(c));
        } else {
          newName.append('-').append(Character.toLowerCase(c));
        }
      } else {
        newName.append(c);
      }
    }
    return newName.toString();
  }
}
