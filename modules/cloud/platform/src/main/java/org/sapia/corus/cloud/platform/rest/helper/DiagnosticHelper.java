package org.sapia.corus.cloud.platform.rest.helper;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.rest.CorusRestClient;

public class DiagnosticHelper {

  private CorusRestClient client;

  public DiagnosticHelper(CorusRestClient client) {
    this.client = client;
  }
  
  public int complete(CorusInstance corus) throws IOException {
    
    WebTarget resource = client.resource("/clusters/" + corus.getCluster().getName() + "/diagnostic");
    
    int status = CorusRestClient.STATUS_DIAGNOSTIC_PENDING;
        
    do {
      Response response = resource.request()
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .get();
      
      status = response.getStatus();
    } while (status == CorusRestClient.STATUS_DIAGNOSTIC_PENDING);

    return status;
  }
}
