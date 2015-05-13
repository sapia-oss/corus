package org.sapia.corus.cloud.platform.workflow;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class DefaultWorkflowLog implements WorkflowLog {

  /**
   * Represents the log verbosity.
   * 
   */
  public enum Level {
    
    VERBOSE(0),
    INFO(1),
    WARNING(2),
    ERROR(3);
    
    private int value;
    
    private Level(int value) {
      this.value = value;
    }
    
    public int value() {
      return value;
    }
    
    public boolean isLoggable(Level given) {
      return given.value >= value;
    }
    
    public static Level forName(String n) {
      char c = n.toUpperCase().charAt(0);
      for (Level l : Level.values()) {
        if(l.name().charAt(0) == c) {
          return l;
        }
      }
      throw new IllegalArgumentException("Unknown log level: " + n);
    }
    
    public static Level getDefault() {
      return Level.INFO;
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Abstracts log output.
   * 
   */
  public interface LogOutput {
    
    public void log(Level l, String msg);
  }

  // --------------------------------------------------------------------------

  /**
   * Implements the {@link LogOutput} interface over the {@link PrintWriter} class.
   * 
   */
  public static class WriterLogOutput implements LogOutput {
    
    private PrintWriter writer;
    
    public WriterLogOutput(PrintWriter writer) {
      this.writer = writer;
    }
    
    @Override
    public void log(Level l, String msg) {
      writer.println(msg);
      writer.flush();
    }
    
  }
  
  // ==========================================================================
  
  private Level     level;
  private LogOutput output;
  
  public DefaultWorkflowLog(Level level, LogOutput output) {
    this.level  = level;
    this.output = output;
  }
  
  @Override
  public void verbose(String msg) {
    log(Level.VERBOSE, msg);
  }
  
  @Override
  public void verbose(String msg, Object... args) {
    log(Level.VERBOSE, msg, args);
  }
  
  @Override
  public void verbose(Throwable err) {
    log(Level.VERBOSE, err);
  }
  
  @Override
  public void info(String msg) {
    log(Level.INFO, msg);
  }
  
  @Override
  public void info(String msg, Object... args) {
    log(Level.INFO, msg, args);
  }
  
  @Override
  public void info(Throwable err) {
    log(Level.INFO, err);
  }
  
  @Override
  public void warning(String msg) {
    log(Level.WARNING, msg);
  }
  
  @Override
  public void warning(String msg, Object... args) {
    log(Level.WARNING, msg, args);
  }
  
  @Override
  public void warning(Throwable err) {
    log(Level.WARNING, err);
  }
  
  @Override
  public void error(String msg) {
    log(Level.ERROR, msg);
  }
  
  @Override
  public void error(String msg, Object... args) {
    log(Level.ERROR, msg, args);
  }
  
  @Override
  public void error(Throwable err) {
    log(Level.ERROR, err);
  }

  // --------------------------------------------------------------------------
  // Factory methods
  
  public static DefaultWorkflowLog forOutput(Level level, LogOutput output) {
    return new DefaultWorkflowLog(level, output);
  }

  public static DefaultWorkflowLog forPrintWriter(Level level, PrintWriter writer) {
    return new DefaultWorkflowLog(level, new WriterLogOutput(writer));
  }
  
  /**
   * @return a new instance of this class that logs to stdout, at the {@link Level#INFO} verbosity.
   */
  public static DefaultWorkflowLog getDefault() {
    return new DefaultWorkflowLog(Level.getDefault(), new WriterLogOutput(new PrintWriter(System.out)));
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private void log(Level given, String msg, Object...args) {
    if (level.isLoggable(given)) {
      output.log(given, String.format(msg, args));
    }
  } 

  private void log(Level given, String msg) {
    if (level.isLoggable(given)) {
      output.log(given, msg);
    }
  } 
  
  private void log(Level given, Throwable err) {
    if (level.isLoggable(given)) {
      output.log(given, errToString(err));
    }
  }
  
  static String errToString(Throwable err) {
    ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
    err.printStackTrace(new PrintWriter(stackTrace, true));
    return stackTrace.toString();
  }
}
