package org.sapia.corus.client.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.sapia.console.CmdElement;
import org.sapia.console.CmdLine;
import org.sapia.console.Option;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * Holds various command-line utility methods.
 * 
 * @author yduchesne
 * 
 */
public final class CliUtils {

  private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[a-zA-Z]:");

  private static final int BUFSZ = 1024;

  private static final int HOST_INDEX = 0;
  private static final int PORT_INDEX = 1;
  
  private CliUtils() {
  }

  /**
   * @param cmd
   *          the {@link CmdLine} to execute.
   * @return the {@link Map} of option values corresponding to the
   *         {@link Option}s of the given {@link CmdLine} instance.
   */
  public static Map<String, String> getOptionsMap(CmdLine cmd) {
    Map<String, String> vars = new HashMap<String, String>();
    for (int i = 0; i < cmd.size(); i++) {
      CmdElement elem = cmd.get(i);
      if (elem instanceof Option) {
        Option opt = (Option) elem;
        if (opt.getValue() != null) {
          vars.put(elem.getName(), ((Option) elem).getValue());
        }
      }
    }
    return vars;
  }

  /**
   * Parses a comma-delimited list of hosts, in the following format:
   * 
   * <pre>
   * host1:port1,host2:port2[,...[,hostN:portN]
   * </pre>
   * 
   * 
   * @param commaDelimitedList
   *          a comma-delimited list of hosts.
   * @return the {@link Set} of {@link ServerAddress}es corresponding to the
   *         given list of hosts.
   */
  public static Set<ServerAddress> parseServerAddresses(String commaDelimitedList) {
    Set<ServerAddress> addresses = new HashSet<ServerAddress>();
    String[] items = commaDelimitedList.split(",");
    for (String item : items) {
      String[] hostPort = item.split(":");
      if (hostPort.length != 2) {
        throw new IllegalArgumentException(String.format("Invalid format for %s. Expected host:port", item));
      }
      addresses.add(HttpAddress.newDefaultInstance(hostPort[HOST_INDEX].trim(), Integer.parseInt(hostPort[PORT_INDEX].trim())));
    }
    return addresses;
  }

  /**
   * 
   * @param corus
   *          the {@link CorusConnectionContextO} instance for which to generate
   *          a command-line prompt.
   * @return a command-line prompt.
   */
  public static final String getPromptFor(CorusConnectionContext context) {
    StringBuffer prompt = new StringBuffer()
        .append("[")
        .append(
            context.getServerHost().getHostName().equals(CorusHost.UNDEFINED_HOSTNAME) ? context.getServerHost().getEndpoint().getServerTcpAddress()
                : context.getServerHost().getHostName() + ":" + context.getServerHost().getEndpoint().getServerTcpAddress().getPort()

        ).append("@").append(context.getDomain()).append("]>> ");
    return prompt.toString();
  }

  /**
   * Extracts all the available data of the passed in input stream and add it to
   * the output stream. When no data is available, it closes the input stream
   * and flush the data of the output stream.
   * 
   * @param anInput
   *          The input stream from which to read the data.
   * @param anOutput
   *          The output stream to which to write the data.
   * @throws IOException
   *           If an error occurs extracting the data.
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
        if (anOutput != null)
          anOutput.flush();
        if (is != null)
          is.close();
      } catch (IOException ioe) {
      }
    }
  }

  /**
   * Extracts the available data of the passed in input stream and add it to the
   * output stream. If no current data is available, it will wait up to the
   * timeout value passed in for data. When all the available data is retrieved
   * or when no data is available and the timeout value is reached, it closes
   * the input stream and flush the data of the output stream.
   * 
   * @param anInput
   *          The input stream from which to read the data.
   * @param anOutput
   *          The output stream to which to write the data.
   * @param aTimeout
   *          The timeout value to stop wainting on available data.
   * @throws IOException
   *           If an error occurs extracting the data.
   */
  public static void extractUntilAvailable(InputStream anInput, OutputStream anOutput, int aTimeout) throws IOException {
    boolean hasRead = false;
    long aStart = System.currentTimeMillis();
    InputStream is = new BufferedInputStream(anInput);

    try {
      byte[] someData = new byte[BUFSZ];
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
        if (anOutput != null)
          anOutput.flush();
        if (is != null)
          is.close();
      } catch (IOException ioe) {
      }
    }
  }

  /**
   * Collects results from different hosts, and puts them in a {@link Map},
   * where each result is bound on a per-host basis.
   * 
   * @param results
   *          a {@link Results} instance.
   * @return the {@link Map} of results, on a per-host basis.
   */
  public static <T> Map<ServerAddress, T> collectResultsPerHost(Results<T> results) {
    Map<ServerAddress, T> resultsPerHost = new HashMap<ServerAddress, T>();
    while (results.hasNext()) {
      Result<T> result = results.next();
      resultsPerHost.put(result.getOrigin().getEndpoint().getServerAddress(), result.getData());
    }
    return resultsPerHost;
  }

  /**
   * @param cmdLine
   *          a {@link CmdLine} instance.
   * @return <code>true</code> if the given instance has the help option
   *         specified.
   */
  public static boolean isHelp(CmdLine cmdLine) {
    return cmdLine.containsOption("help", false) || cmdLine.containsOption("-help", false);
  }
 
  /**
   * @param optionName the name of the option from which to split.
   * @param toSplit the {@link CmdLine} instance to split.
   * @return the {@link CmdLine} consisting of the elements starting from the option with the 
   * given name - the {@link CmdLine#size()} method of the returned {@link CmdLine} will return 0
   * if no option corresponding to the given name was found.
   */
  public static CmdLine fromOption(String optionName, CmdLine toSplit) {
    CmdLine cmd = new CmdLine();
    boolean gather = false;
    int i = 0;
    for (; i  < toSplit.size(); i++) {
      CmdElement elem = toSplit.get(i);
      if (elem instanceof Option) {
        if (elem.getName().equals(optionName)) {
          cmd.addOpt((Option) elem);
          gather = true;
          break;
        }
      }
    }
    i++;
    for (; i < toSplit.size() && gather; i++) {
      cmd.addElement(toSplit.get(i)); 
    }
    return cmd;
  } 
  
  /**
   * @param optionName the name of the option until which to split.
   * @param toSplit the {@link CmdLine} instance to split.
   * @return the {@link CmdLine} consisting of the elements up to the option  with the given 
   * name - and excluding that option. 
   */
  public static CmdLine toOption(String optionName, CmdLine toSplit) {
    CmdLine cmd = new CmdLine();
    for (int i = 0; i < toSplit.size(); i++) {
      CmdElement elem  = toSplit.get(i);
      if (elem instanceof Option) {
        if (elem.getName().equals(optionName)) {
          break;
        }
      } 
      cmd.addElement(elem);
    }
    return cmd;
  }
  
  /**
   * @param fileName
   *          a file name.
   * @return <code>true</code> if the given name corresponding to an absolute
   *         file.
   */
  public static boolean isAbsolute(String fileName) {
    String theFileName = fileName.trim();
    return theFileName.length() > 0 && (WINDOWS_DRIVE_PATTERN.matcher(theFileName).find() || theFileName.startsWith("/"));
  }
}
