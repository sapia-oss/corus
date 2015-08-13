package org.sapia.corus.client.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.common.StrLookups;
import org.sapia.corus.client.services.security.Permission;

/**
 * Executes a script on the server-side.
 * 
 * @author yduchesne
 *
 */
public class ScriptResource {

  @Path({
    "/runscript"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.TEXT_PLAIN})
  @Authorized(Permission.ADMIN)
  public ProgressResult execCorusScript(RequestContext ctx) throws IOException {
    File currentBaseDir = ctx.getConnector().getContext().getFileSystem().getBaseDir();
    try {
      return doRunCorusScript(ctx);
    } finally {
      ctx.getConnector().getContext().getFileSystem().changeBaseDir(currentBaseDir.getAbsolutePath());
    }
  } 
  
  private ProgressResult doRunCorusScript(RequestContext ctx) throws IOException {
    
    boolean     clusteringEnabled = ctx.getRequest().getValue("clusteringEnabled", "true").asBoolean();
    String      encoding          = ctx.getRequest().getValue("encoding", "UTF-8").asString();
    Interpreter interpreter       = new Interpreter(ctx.getConnector());
    if (!clusteringEnabled) {
      interpreter.setAutoClusterInfo(AutoClusterFlag.notClustered());
    }
    
    final List<String> messages = new ArrayList<String>();
    
    ConsoleOutput out = new ConsoleOutput() {
      
      @Override
      public void println(String s) {
        messages.add(s);
      }
      
      @Override
      public void println() {
      }      
      @Override
      public void print(char c) {
      }
      
      @Override
      public void print(String s) {
      }
      
      @Override
      public void flush() {
      }
    };
    
    interpreter.setOut(out);
    
    InputStream         stream       = ctx.getRequest().getContent();
    try {
      InputStreamReader streamReader = new InputStreamReader(stream, encoding);
      interpreter.interpret(streamReader, StrLookups.merge(
          StrLookup.systemPropertiesLookup(),
          StrLookup.mapLookup(System.getenv())
      ));
    } catch (Throwable t) {
      if (t instanceof InvocationTargetException) {
        return new ProgressResult(messages, ((InvocationTargetException) t).getCause());
      } 
      return new ProgressResult(messages, t);
    } finally {
      stream.close();
    }
    return new ProgressResult(messages);
  }
}
