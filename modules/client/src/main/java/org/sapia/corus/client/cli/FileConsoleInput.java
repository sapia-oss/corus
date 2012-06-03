package org.sapia.corus.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.sapia.console.ConsoleInput;

/**
 * Implements a {@link ConsoleInput} that reads commands from a file.
 * 
 * @author yduchesne
 *
 */
public class FileConsoleInput implements ConsoleInput {
  
  private BufferedReader reader;
  
  /**
   * @param file the {@link File} to read input from.
   * @throws IOException if the given file could not be opened.
   */
  public FileConsoleInput(File file) throws IOException {
    this.reader = new BufferedReader(new FileReader(file));
  }
  
  @Override
  public String readLine() throws IOException {
    return reader.readLine();
  }
  
  @Override
  public char[] readPassword() throws IOException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Closes this instance's file.
   */
  public void close() {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        // noop
      }
    }
  }
  

}
