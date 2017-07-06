package org.sapia.corus.ftest.rest.stress;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.TimeValue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PropertiesResourcesStressTest {

  private static final int       THREADS = 25;
  private static final int       RUNS    = 10;
  private static final TimeValue PAUSE   = TimeValue.createMillis(10);
  
  private StressTestTaskRunner runner;
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
    runner = new StressTestTaskRunner(THREADS);
  }
  
  @AfterMethod
  public void afterMethod() {
    runner.shutdown();
    
    FtestClient client = FtestClient.open();
    try {
      client.getConnector().getConfigFacade().removeProperty( 
          PropertyScope.PROCESS, 
          ArgMatchers.parse("test.prop.*"),
          new HashSet<ArgMatcher>(),
          ClusterInfo.clustered()
      );
      
      client.getConnector().getConfigFacade().removeProperty( 
          PropertyScope.PROCESS, 
          ArgMatchers.parse("test.prop.*"),
          Collects.arrayToSet(ArgMatchers.parse("test.*")),
          ClusterInfo.clustered()
      );
    } finally {
      client.close();
    }
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testAddProperties_clustered() throws Exception {
    StressTestStatus status = runner.run(() -> new StressTestTask(RUNS, PAUSE) {
      @Override
      protected void doRun(FtestClient client) throws Exception {
        JSONValue response = client.resource("/clusters/ftest/properties/process")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
          assertEquals(200, response.asObject().getInt("status"));
      }
    });
    
    System.out.print(status);
    
  }
}
