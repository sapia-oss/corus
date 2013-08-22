package org.sapia.corus.client.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * Holds various command-line utility methods.
 * 
 * @author yduchesne
 *
 */
public final class CliUtils {
  
  private static int BUFSZ = 1024;
  
  private CliUtils() {
  }
  
  /**
   * Parses a comma-delimited list of hosts, in the following format:
   * <pre>
   * host1:port1,host2:port2[,...[,hostN:portN]
   * </pre>
   * 
   * 
   * @param commaDelimitedList a comma-delimited list of hosts.
   * @return the {@link Set} of {@link ServerAddress}es corresponding to the given list of hosts.
   */
  public static Set<ServerAddress> parseServerAddresses(String commaDelimitedList) {
    Set<ServerAddress> addresses = new HashSet<ServerAddress>();
    String[] items = commaDelimitedList.split(",");
    for (String item : items) {
      String[] hostPort = item.split(":");
      if (hostPort.length != 2) {
        throw new IllegalArgumentException(String.format("Invalid format for %s. Expected host:port", item));
      }
      addresses.add(HttpAddress.newDefaultInstance(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim())));
    }
    return addresses;
  }

  /**
   * 
   * @param corus the {@link CorusConnectionContext} instance for which to generate a command-line prompt.
   * @return a command-line prompt.
   */
  public static final String getPromptFor(CorusConnectionContext context) {
    StringBuffer prompt = new StringBuffer()
    .append("[")
    .append(context.getAddress().toString())
    .append("@")
    .append(context.getDomain())
    .append("]>> ");
    return prompt.toString();
  }
  
  /**
   * Extracts all the available data of the passed in input stream and add it
   * to the output stream. When no data is available, it closes the input stream and
   * flush the data of the output stream.
   * 
   * @param anInput The input stream from which to read the data.
   * @param anOutput The output stream to which to write the data.
   * @throws IOException If an error occurs extracting the data.
   */
  public static void extractAvailable(InputStream anInput, OutputStream anOutput) throws IOException {
    InputStream is = new BufferedInputStream(anInput);
    try {
      byte[] someData = new byte[BUFSZ];
      int length = 0;
      int size = is.available();
      while (size > 0) {
        if (size > someData.length) {
          length = is.read(someData, 0, someData.length);
        } else {
          length = is.read(someData, 0, size);
        }
        anOutput.write(someData, 0, length);
        size = is.available();
      } 
    } finally {
      try {
        if (anOutput != null) anOutput.flush();
        if (is != null) is.close();
      } catch (IOException ioe) {
      }
    }
  }
  
  /**
   * Extracts the available data of the passed in input stream and add it to the output
   * stream. If no current data is available, it will wait up to the timeout value passed
   * in for data. When all the available data is retrieved or when no data is available and
   * the timeout value is reached, it closes the input stream and flush the data of the
   * output stream.
   * 
   * @param anInput The input stream from which to read the data.
   * @param anOutput The output stream to which to write the data.
   * @param aTimeout The timeout value to stop wainting on available data.
   * @throws IOException If an error occurs extracting the data.
   */
  public static void extractUntilAvailable(InputStream anInput, OutputStream anOutput, int aTimeout) throws IOException {
    boolean hasRead = false;
    long aStart = System.currentTimeMillis();
    InputStream is = new BufferedInputStream(anInput);

    try {
      byte[] someData = new byte[1024];
      while (!hasRead) {
        int length = 0;
        int size = is.available();
        while (size > 0) {
          if (size > someData.length) {
            length = is.read(someData, 0, someData.length);
          } else {
            length = is.read(someData, 0, size);
          }
          hasRead = true;
          anOutput.write(someData, 0, length);
          size = is.available();
        } 
        
        if (!hasRead && ((System.currentTimeMillis() - aStart) <= aTimeout)) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException ie) {
          }
        } else {
          break;
        }
      } 
    } finally {
      try {
        if (anOutput != null) anOutput.flush();
        if (is != null) is.close();
      } catch (IOException ioe) {
      }
    }
  }  
}
