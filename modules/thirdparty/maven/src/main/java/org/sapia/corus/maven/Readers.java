package org.sapia.corus.maven;

import java.io.BufferedReader;
import java.io.IOException;

public class Readers {

  public static void transfertTo(BufferedReader reader, StringBuilder builder) throws IOException {
    String line = null;
    while (reader.ready()) {
      line = reader.readLine();
      builder.append(line + "\n");
    }
  }

}
