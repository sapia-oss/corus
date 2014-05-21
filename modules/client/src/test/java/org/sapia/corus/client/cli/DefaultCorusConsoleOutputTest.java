package org.sapia.corus.client.cli;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.client.cli.CorusConsoleOutput.DefaultCorusConsoleOutput;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCorusConsoleOutputTest {
  
  @Mock
  private ConsoleOutput delegate;
  
  @Mock
  private CorusConsoleOutput output; 
  
  @Before
  public void setUp() {
    output = DefaultCorusConsoleOutput.wrap(delegate);
  }
  
  @Test
  public void testPrintlnStringOff() {
    output.turnOff();
    output.println("test");
    verify(delegate, never()).println(anyString());
  }
  
  @Test
  public void testPrintlnStringOn() {
    output.turnOn();
    output.println("test");
    verify(delegate).println("test");
  }

  @Test
  public void testPrintlnOff() {
    output.turnOff();
    output.println();
    verify(delegate, never()).println();
  }
  
  @Test
  public void testPrintlnOn() {
    output.turnOn();
    output.println();
    verify(delegate).println();
  }

  @Test
  public void testPrintStringOff() {
    output.turnOff();
    output.print("");
    verify(delegate, never()).println(anyString());
  }
  
  @Test
  public void testPrintStringOn() {
    output.turnOn();
    output.print("");
    verify(delegate, never()).println("");
  }

  @Test
  public void testPrintCharOff() {
    output.turnOff();
    output.print('c');
    verify(delegate, never()).print(anyChar());
  }

  @Test
  public void testPrintCharOn() {
    output.turnOn();
    output.print('c');
    verify(delegate).print('c');
  }
  
  @Test
  public void testFlushOff() {
    output.turnOff();
    output.flush();
    verify(delegate, never()).flush();    
  }

  @Test
  public void testFlushOn() {
    output.turnOn();
    output.flush();
    verify(delegate).flush();    
  }
}
