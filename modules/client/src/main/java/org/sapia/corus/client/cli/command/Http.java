package org.sapia.corus.client.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * This command provides various HTTP-related utilities:
 * 
 * <li>
 * <ul>
 * The ability to check given HTTP endpoints (either by specifying URLs, or by
 * specifying port ranges).
 * <ul>
 * The ability to perform a HTTP post on a given URL.</li>
 * 
 * @author yduchesne
 * 
 */
public class Http extends CorusCliCommand {

  private static final String CHECK_ARG = "check";
  private static final String POST_ARG  = "post";
  
  private static final OptionDef URL_OPT            = new OptionDef("u", true);
  private static final OptionDef MAX_ATTEMPTS_OPT   = new OptionDef("m", true);
  private static final OptionDef INTERVAL_OPT       = new OptionDef("t", true);
  private static final OptionDef STATUS_OPT         = new OptionDef("s", true);
  private static final OptionDef PORT_RANGE_OPT     = new OptionDef("p", true);
  private static final OptionDef CONTEXT_PATH_OPT   = new OptionDef("c", true);
  private static final OptionDef PREFIX_OPT         = new OptionDef("x", true);
  private static final OptionDef EXPECTED_INSTANCES = new OptionDef("i", true);
  
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      URL_OPT, MAX_ATTEMPTS_OPT, INTERVAL_OPT, STATUS_OPT, 
      PORT_RANGE_OPT, CONTEXT_PATH_OPT, PREFIX_OPT, EXPECTED_INSTANCES,
      OPT_CLUSTER
  );

  private static final int DEFAULT_STATUS             = 200;
  private static final int DEFAULT_MAX_ATTEMPTS       = 12;
  private static final int DEFAULT_INTERVAL           = 5;
  private static final int DEFAULT_EXPECTED_INSTANCES = 1;

  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    if (ctx.getCommandLine().isNextArg()) {
      String subCommand = ctx.getCommandLine().assertNextArg().getName();
      if (subCommand.equalsIgnoreCase(CHECK_ARG)) {
        doCheck(ctx);
      } else if (subCommand.equalsIgnoreCase(POST_ARG)) {
        doPost(ctx);
      } else {
        throw new InputException("Unknown argument");
      }

    } else {
      throw new InputException("Missing argument");
    }
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  // --------------------------------------------------------------------------
  // subcommand methods

  private void doCheck(CliContext ctx) throws InputException {
    int maxAttempts = getOpt(ctx, MAX_ATTEMPTS_OPT.getName(), "" + DEFAULT_MAX_ATTEMPTS).asInt();
    int interval = getOpt(ctx, INTERVAL_OPT.getName(), "" + DEFAULT_INTERVAL).asInt();
    int expected = getOpt(ctx, STATUS_OPT.getName(), "" + DEFAULT_STATUS).asInt();
    int instances = getOpt(ctx, EXPECTED_INSTANCES.getName(), "" + DEFAULT_EXPECTED_INSTANCES).asInt();

    if (ctx.getCommandLine().containsOption(URL_OPT.getName(), true)) {
      String url = ctx.getCommandLine().assertOption(URL_OPT.getName(), true).getValue();
      doCheck(ctx, url, maxAttempts, expected, interval);
    } else if (ctx.getCommandLine().containsOption(PORT_RANGE_OPT.getName(), false)) {
      String contextPath = getOptValue(ctx, CONTEXT_PATH_OPT.getName());

      ClusterInfo cluster;
      
      if (!ctx.getCommandLine().containsOption(OPT_CLUSTER.getName(), false)) {
        cluster = new ClusterInfo(true);
        cluster.addTarget(ctx.getCorus().getContext().getServerHost().getEndpoint().getServerTcpAddress());
      } else {
        cluster = getClusterInfo(ctx);
      }

      Map<ServerAddress, List<PortRange>> portRangesByNode = CliUtils.collectResultsPerHost(ctx.getCorus().getPortManagementFacade()
          .getPortRanges(cluster));

      Map<ServerAddress, List<Distribution>> distsByNode = CliUtils.collectResultsPerHost(ctx.getCorus().getDeployerFacade()
          .getDistributions(DistributionCriteria.builder().all(), cluster));

      Map<ServerAddress, Set<Tag>> tagsByNode = CliUtils.collectResultsPerHost(ctx.getCorus().getConfigFacade().getTags(cluster));

      List<ArgMatcher> portRangePatterns = getOptValues(ctx, PORT_RANGE_OPT.getName(), new Func<ArgMatcher, String>() {
        @Override
        public ArgMatcher call(String arg) {
          return ArgMatchers.parse(arg);
        }
      });

      if (portRangesByNode.isEmpty()) {
        ctx.getConsole().println("Found not port ranges that apply: bypassing HTTP check");
      } else {
        String portPrefix = getOptValue(ctx, PREFIX_OPT.getName());
        for (ServerAddress node : portRangesByNode.keySet()) {
          List<PortRange> ranges = portRangesByNode.get(node);
          for (PortRange r : ranges) {
            if (isIncluded(portRangePatterns, r)) {
              List<ProcessCriteria> processSelectors = getProcessConfigsForRange(ctx, r, Collects.emptyIfNull(distsByNode.get(node)), Collects.emptyIfNull(tagsByNode.get(node)));
              if (!processSelectors.isEmpty()) {
                for (ProcessCriteria s : processSelectors) {
                  ctx.getConsole().println("Will check processes running using port range: " + r.getName());
                  waitForRunningProcesses(ctx, node, s, TimeUnit.MILLISECONDS.convert(interval, TimeUnit.SECONDS), maxAttempts, instances);
                  HttpAddress address = (HttpAddress) node;
                  for (Integer port : r.getActive()) {
                    String url = "http://" + address.getHost() + ":" + (portPrefix != null ? portPrefix + port : port);
                    url = FileUtils.append(url, contextPath);
                    doCheck(ctx, url, maxAttempts, expected, interval);
                  }
                } 
              }
            } else {
              ctx.getConsole().println("Found not port ranges that apply: bypassing HTTP check");
            }
          }
        }
      }
    } else {
      throw new InputException("-u or -p option must be specified");
    }
  }
  
  static void doPost(CliContext ctx) throws InputException {
    String url = ctx.getCommandLine().assertOption(URL_OPT.getName(), true).getValue();
    int expected = -1;
    if (ctx.getCommandLine().containsOption(STATUS_OPT.getName(), true)) {
      expected = ctx.getCommandLine().assertOption(STATUS_OPT.getName(), true).asInt();
    }

    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(url);
    try {
      HttpResponse response = client.execute(post);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != expected && expected > -1) {
        throw new AbortException(String.format("Error: expected status code %s. Got: %s", expected, statusCode));
      }
      EntityUtils.consume(response.getEntity());
    } catch (IOException e) {
      throw new AbortException("IO error occurred", e);
    }
  }

  // --------------------------------------------------------------------------
  // utility methods

  static void doCheck(CliContext ctx, String url, int maxAttempts, int expected, int interval) {
    ctx.getConsole().println(String.format("Checking HTTP endpoint: %s. Expecting status code: %s", url, expected));

    HttpClient client = new DefaultHttpClient();

    int count = 0;
    long start = System.currentTimeMillis();
    while (count < maxAttempts) {
      HttpGet get = new HttpGet(url);
      try {
        HttpResponse response = client.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == expected) {
          long durationSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
          ctx.getConsole().println(String.format("Expected response received from server at %s after %s seconds.", url, durationSeconds));
          break;
        } else if (count >= maxAttempts - 1) {
          long durationSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
          throw new IllegalStateException(String.format(
              "Unexpected response received from server at %s (check duration: %s seconds). Got HTTP status code: %s", url, durationSeconds,
              statusCode));
        }
        EntityUtils.consume(response.getEntity());
      } catch (Exception e) {
        if (count >= maxAttempts - 1) {
          long durationSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
          throw new IllegalStateException(String.format("Server not responding properly: %s. Aborting (check duration: %s seconds).", url,
              durationSeconds));
        }
      }
      count++;
      sleep(TimeUnit.MILLISECONDS.convert(interval, TimeUnit.SECONDS));
    }
  }
  
  static List<ProcessCriteria> getProcessConfigsForRange(CliContext ctx, PortRange r, List<Distribution> nodeDists, Set<Tag> nodeTags) {
    List<ProcessCriteria> configs = new ArrayList<>();
    Set<Tag> corusTags = new HashSet<Tag>();
    if (nodeTags != null) {
      corusTags.addAll(nodeTags);
    }

    if (nodeDists != null) {
      for (Distribution d : nodeDists) {
        for (ProcessConfig p : d.getProcesses()) {
          for (org.sapia.corus.client.services.deployer.dist.Port pt : p.getPorts()) {
            if (r.getName().equals(pt.getName())) {
              Set<Tag> processTags = new HashSet<Tag>();
              processTags.addAll(Tag.asTags(d.getTagSet()));
              processTags.addAll(Tag.asTags(p.getTagSet()));
              if(processTags.isEmpty() || corusTags.containsAll(processTags)) {
                ProcessCriteria criteria = ProcessCriteria.builder()
                    .distribution(d.getName())
                    .version(d.getVersion())
                    .name(p.getName())
                    .build();
                configs.add(criteria);
              }
            }
          }
        }
      }
    }

    return configs;
  }
  
  static void waitForRunningProcesses(
      CliContext      ctx, 
      ServerAddress   currentHost, 
      ProcessCriteria criteria, 
      long intervalMillis, int maxAttempts, int numberOfExpectedProcesses) {
    
    int         attempts = 0;
    ClusterInfo cluster  = new ClusterInfo(true);
    cluster.addTarget(currentHost);
    
    Set<org.sapia.corus.client.services.processor.Process> running = new HashSet<>();
    
    if (numberOfExpectedProcesses > 0) {
      ctx.getConsole().println("HTTP check waiting for " + numberOfExpectedProcesses + " process(es) to start");
    }
    
    while (attempts < maxAttempts && running.size() < numberOfExpectedProcesses) {
      Results<List<org.sapia.corus.client.services.processor.Process>> tmp = ctx.getCorus().getProcessorFacade().getProcesses(criteria, cluster);
      for (Result<List<org.sapia.corus.client.services.processor.Process>> d : tmp) {
        running.addAll(d.getData());
      }
      sleep(intervalMillis);
      attempts++;
    }
    
    if (running.size() < numberOfExpectedProcesses) {
      throw new AbortException(String.format("Number of expected running processes not respected for: %s:%s:%s. Expected %s processes, got %s", 
          criteria.getDistribution(), criteria.getVersion(), criteria.getName(), numberOfExpectedProcesses, running.size()));
    }
  }

  static boolean isIncluded(List<ArgMatcher> portRangePatterns, PortRange r) {
    if (portRangePatterns.isEmpty()) {
      return true;
    } else {
      for (ArgMatcher p : portRangePatterns) {
        if (p.matches(r.getName())) {
          return true;
        }
      }
      return false;
    }
  }

}
