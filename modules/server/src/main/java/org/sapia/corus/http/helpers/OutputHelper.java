package org.sapia.corus.http.helpers;

import simple.http.Request;
import simple.http.Response;

/**
 * This interface specifies the behavior of classes whose instance output
 * content to a HTTP response, given a request.
 * 
 * @author yduchesne
 *
 */
public interface OutputHelper {
	
  public void print(Request req, Response res) throws Exception;
  
}
