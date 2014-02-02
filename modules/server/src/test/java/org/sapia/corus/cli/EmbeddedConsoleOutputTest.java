package org.sapia.corus.cli;

import static org.mockito.Mockito.*;

import org.apache.log.Hierarchy;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedConsoleOutputTest {
  
  private Logger                logger;
  private EmbeddedConsoleOutput output;
  
  @Mock
  private LogTarget             target;
  
  @Before
  public void setUp() {
    logger = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    logger.setLogTargets(new LogTarget[] { target });
    output = new EmbeddedConsoleOutput(logger);
  }

  
  @Test
  public void testFlushAfterPrintChar() {
    output.print('c');
    output.flush();
    verify(target, times(1)).processEvent(any(LogEvent.class));
  }
  
  @Test
  public void testPrintLine() {
    output.println("test");
    verify(target, times(1)).processEvent(any(LogEvent.class));
  }  
  
  @Test
  public void testPrintEmptyLine() {
    output.println();
    verify(target, times(1)).processEvent(any(LogEvent.class));
  }
  
  @Test
  public void testFlushWhenNotOutput() {
    output.flush();
    verify(target, never()).processEvent(any(LogEvent.class));
  }
  
  @Test
  public void testPrintCharFlush() {
    output.print('c');
    output.flush();
    verify(target).processEvent(argThat(logMatcher("c")));
  }
  
  @Test
  public void testPrintLineFlush() {
    output.println("test");
    verify(target).processEvent(argThat(logMatcher("test")));
  }
  
  @Test
  public void testPrintLineWithExistingContentFlush() {
    output.print('t');    
    output.println("est");
    verify(target).processEvent(argThat(logMatcher("test")));
  }    
  
  private ArgumentMatcher<LogEvent> logMatcher(final String expectedOutput) {
    return new ArgumentMatcher<LogEvent>() {
      @Override
      public boolean matches(Object argument) {
        LogEvent evt = (LogEvent) argument;
        return evt.getMessage().equalsIgnoreCase(expectedOutput);
      }
    };
  }

}
