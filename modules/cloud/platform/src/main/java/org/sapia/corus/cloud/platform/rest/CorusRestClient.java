package org.sapia.corus.cloud.platform.rest;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;

public interface CorusRestClient {

  int STATUS_OK                 = 200;
  int STATUS_IN_PROGRESS        = 250;
  int STATUS_ERROR_IN_PROGRESS  = 251;
  int STATUS_PARTIAL_SUCCESS    = 252;
  int STATUS_SERVER_ERROR       = 500;
  int STATUS_DIAGNOSTIC_PENDING = 503;

  String HEADER_APP_ID  = "X-corus-app-id";
  String HEADER_APP_KEY = "X-corus-app-key";

  /**
   * Invoke in order to execute a HTTP PUT.
   *
   * @param path a resource path.
   * @return a new {@link WebTarget} instance.
   * @throws IOException if an I/O error occurs.
   */
  WebTarget resource(String path) throws IOException;

  /**
   * @param path a resource path.
   * @return the URL string for the given path.
   */
  String url(String path);
  
  /**
   * Closes this instance.
   */
  public void close();

}