package org.sapia.corus.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.sapia.console.ConsoleInput;

public class FileConsoleInput implements ConsoleInput {
  
  private BufferedReader reader;
  
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

}
