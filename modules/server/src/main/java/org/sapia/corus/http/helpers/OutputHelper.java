package org.sapia.corus.http.helpers;

import org.sapia.corus.client.services.http.HttpRequestFacade;
import org.sapia.corus.client.services.http.HttpResponseFacade;

/**
 * This interface specifies the behavior of classes whose instance output
 * content to a HTTP response, given a request.
 * 
 * @author yduchesne
 *
 */
public interface OutputHelper {
	
  public void print(HttpRequestFacade req, HttpResponseFacade res) throws Exception;
  
}
