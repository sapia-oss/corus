package org.sapia.corus.cli;

import java.io.File;

import org.apache.log4j.Level;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.cli.DefaultClientFileSystem;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.facade.CorusConnectorImpl;

/**
 * An embedded interpreter.
 * 
 * @author yduchesne
 *
 */
public class EmbeddedInterpreter extends Interpreter {
  
  /**
   * @param corus the server-side {@link Corus} instance.
   * @param basedir the {@link File} corresponding to the interpreter's working directory.
   */
  public EmbeddedInterpreter(Corus corus, File basedir) {
    super(
        new EmbeddedConsoleOutput(), 
        new CorusConnectorImpl(
            new EmbeddedCorusConnectionContext(corus, new DefaultClientFileSystem(basedir))
        )
    );
  }
  
  @Override
  protected void disableLogging() {
  }
  
  @Override
  protected void enableLogging(Level level) {
  }

}
