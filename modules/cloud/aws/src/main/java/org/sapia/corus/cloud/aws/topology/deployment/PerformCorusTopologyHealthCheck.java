package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpStatus;
import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.domain.CorusAddress;
import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.http.Headers;
import org.sapia.corus.cloud.platform.http.HttpClientFacade;
import org.sapia.corus.cloud.platform.http.HttpResponse;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.platform.workflow.exceptions.AbortedDeploymentException;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Region;
import org.sapia.corus.cloud.topology.Topology;
import org.sapia.corus.cloud.topology.Zone;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

/**
 * Performs health check of Corus instances that will be used for deployment (one instance per cluster).
 * 
 * @author yduchesne
 *
 */
public class PerformCorusTopologyHealthCheck implements WorkflowStep<AwsTopologyDeploymentContext> {
  
  private static final String DESC = "performing pre-deployment diagnostic";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
    Topology topology = context.getSettings().getNotNull("topology").get(Topology.class);
    String   envName  = context.getSettings().getNotNull("environment").get(String.class);
    Env      env      = topology.getEnvByName(envName);
   
    for (Region region : env.getRegions()) {
      for (Zone zone : region.getZones()) {
        for (Cluster cluster : env.getClusters()) {
          List<Filter> filters = new ArrayList<Filter>();
          filters.add(new Filter("corus.environment", Arrays.asList(env.getName())));
          filters.add(new Filter("corus.region", Arrays.asList(region.getName())));
          filters.add(new Filter("corus.zone", Arrays.asList(zone.getName())));
          filters.add(new Filter("corus.domain", Arrays.asList(cluster.getName())));
          filters.add(new Filter("corus.topology.org", Arrays.asList(topology.getOrg())));
          filters.add(new Filter("corus.topology.application", Arrays.asList(topology.getApplication())));
          context.getLog().info(
              "Will perform diagnostic on cluster %s in region/zone %s/%s of environment %s", 
              cluster.getName(), region.getName(), zone.getName(), env.getName()
          );
          doPerformClusterDiagnostic(context, region, zone, env, cluster, filters);
        }
      }
    }
  }
  
  private void doPerformClusterDiagnostic(AwsTopologyDeploymentContext context, 
      Region region, Zone zone, Env env, Cluster cluster,
      List<Filter> filters) throws Exception {

    DescribeInstancesResult descRes = context.getEc2Client().describeInstances(
        new DescribeInstancesRequest().withFilters(filters)
    );
        
    List<Instance> instances = new ArrayList<Instance>();
    
    for (Reservation reservation : descRes.getReservations()) {
      for (Instance instance : reservation.getInstances()) {
        if (instance.getState().getCode().equals(InstanceStatusCode.RUNNING.value())) {
          instances.add(instance);
        } 
      }
    }
   
    Preconditions.checkState(!instances.isEmpty(), "No instance(s) found running");
    
    context.getLog().info("Attempting to find Corus host to connect to in order to perform diagnostic");

    // 1) trying to find repo instance with public IP
    Collection<Instance> corusHosts = filterPublicRepoServerInstances(instances);
   
    // 2) if none, try to find instance with public IP
    if (corusHosts.isEmpty()) {
      context.getLog().info("Did not find Corus repo server node with public IP; trying to find repo client node with public IP");
      corusHosts = filterPublicInstances(instances);
    }
    // 3) if none, try to find repo instance with non-public IP
    if (corusHosts.isEmpty()) {
      context.getLog().warning(
          "Did not find instance with public IP to connect to for cluster %s (your topology has " 
          + "no <machine> element with the publicIpEnabled attribute set to true). " 
          + "Will resort to try to connect to instance with private IP. "
          + "This will fail if your network setup does not support such connectivity with AWS.", cluster.getName());
      corusHosts = filterRepoServerInstances(instances);
    }
   
    // 4) if none, use whatever
    if (corusHosts.isEmpty()) {
      context.getLog().info("No Corus repo server node with private IP found. Falling back to any Corus node available");

      corusHosts = instances;
    }
    // using first instance, arbitrarily
    Instance toCheck = corusHosts.iterator().next();
    String host = 
        Strings.isNullOrEmpty(toCheck.getPublicIpAddress()) 
        ? toCheck.getPrivateIpAddress() 
        : toCheck.getPublicIpAddress();
    int port = context.getSettings().getNotNull("corusPort").get(Integer.class);
    CorusAddress corusAddress = new CorusAddress(host, port);
    String corusUrl = corusAddress.asHttpsUrl() + "/rest/clusters/" + cluster.getName() + "/diagnostic";
      
    HttpClientFacade httpClient        = context.getHttpClientFactory().getClient();
    TimeMeasure      httpClientTimeout = context.getSettings().getNotNull("httpClientTimeout").get(TimeMeasure.class);
    TimeMeasure      pollingInterval   = context.getSettings().getNotNull("pollingInterval").get(TimeMeasure.class);
    int              corusMaxConnectRetry = context.getSettings().getNotNull("corusMaxConnectRetry").get(Integer.class);
    HttpResponse     response          = null;   
    int              statusCode        = HttpResponse.STATUS_DIAGNOSTIC_PENDING;
    String           statusMessage     = null;
    int              retryCount        = 0;
    do {
      try{
        CorusCredentials credentials = context.getSettings()
            .getNotNull("corusCredentials")
            .get(CorusCredentials.class);
        response = httpClient.get(
            corusUrl, 
            httpClientTimeout.getMillis(), 
            Headers.of(                
                CorusCredentials.HEADER_APP_ID, credentials.getAppId(),
                CorusCredentials.HEADER_APP_KEY, credentials.getAppKey()
            ).asMap()
        );
        statusCode    = response.getStatusCode();
        statusMessage = response.getStatusMessage();
        if (statusCode == HttpResponse.STATUS_OK) {
          context.getLog().info("Connection to Corus instance at address %s was successful for cluster %s", corusAddress, cluster.getName());
          context.addCorusInstance(new CorusInstance(region, zone, env, cluster, corusAddress));
        } else if (statusCode == HttpResponse.STATUS_DIAGNOSTIC_PENDING) {
          context.getLog().warning("Diagnostic pending at %s (retrying...)", corusAddress);         
        } else {
          context.getLog().warning(
             "Connection to Corus instance at %s failed for cluster %s: %s (status code = %s)", 
             corusAddress,  cluster.getName(), response.getStatusMessage(), response.getStatusCode()
          );
        }
      } catch (IOException e) {
        if (retryCount < corusMaxConnectRetry) {
          context.getLog().warning(
              "Connection to Corus instance at %s failed for cluster %s: %s (retrying...)", 
             corusAddress,  cluster.getName(), e.getMessage()
          );
          retryCount++;
        } else {
          throw new AbortedDeploymentException("Could not connect to Corus at cluster: " + cluster.getName(), e);
        }
      } finally {
        if (response != null) {
          response.close();
        }
        httpClient.close();
      }
      
    } while (context.getTimeSupplier().sleepConditionally(pollingInterval, statusCode == HttpResponse.STATUS_DIAGNOSTIC_PENDING));
    
    if (response == null) {
      throw new AbortedDeploymentException("Could not connect to Corus at cluster: " + cluster.getName() + ". Check logs for details");
    } else if (statusCode != HttpStatus.SC_OK) {
      throw new AbortedDeploymentException(String.format(
          "Diagnostic failed at %s for cluster %s, cannot deploy. Status message: %s. Status code: %s", 
          corusAddress, cluster.getName(),
          statusMessage, statusCode
      ));
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  @VisibleForTesting
  Collection<Instance> filterPublicRepoServerInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        for (Tag t : input.getTags()) {
          if (t.getKey().equals("corus.repo.role") 
              && t.getValue().equals("server")
              && !Strings.isNullOrEmpty(input.getPublicIpAddress())) {
            return true;
          }
        }
        return false;
      }
    });    
  }
  
  @VisibleForTesting
  Collection<Instance> filterRepoServerInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        for (Tag t : input.getTags()) {
          if (t.getKey().equals("corus.repo.role") && t.getValue().equals("server")) {
            return true;
          }
        }
        return false;
      }
    });    
  }
  
  @VisibleForTesting
  private Collection<Instance> filterPublicInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        return !Strings.isNullOrEmpty(input.getPublicIpAddress());
      }
    });    
  }
  
}
