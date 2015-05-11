package org.sapia.corus.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Base class for common attributes of Corus maven plugin.
 *
 * @author jcdesrochers
 */
public abstract class BaseCorusMojoSupport extends AbstractMojo {

  /**
   * Indicates if the executed corus script can be skipped or not.
   */
  @Parameter(property = "skip", defaultValue = "false")
  private boolean skipExecution;

  /**
   * Protected constructor.
   */
  protected BaseCorusMojoSupport() {
    super();
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // Process skip flag
    if (skipExecution) {
      getLog().info("Skip execution flag is active: skipping execution of this goal");
      return;
    } else {
      doExecute();
    }
  }

  /**
   * Delegated execute method for subclasses.
   *
   * @throws MojoExecutionException If an error occurs in the execution.
   * @throws MojoFailureException If a system failure occurs in the execution.
   */
  protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
