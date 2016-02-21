package org.sapia.corus.ftest.rest.core;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ScriptResourceFuncTest {

  private FtestClient client;
  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
  }
  
  @AfterSuite
  public void afterSuite() throws Exception {
    client.close();
  }
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
  }
  
  @Test
  public void testExecCorusScript() throws Exception {
    
    JSONValue response = client.resource("/runscript")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("echo \"test\"", MediaType.TEXT_PLAIN), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

  }
}
