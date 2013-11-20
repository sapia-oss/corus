package org.sapia.corus.client.services.http;

/**
 * Encapsulates HTTP request/response data.
 * 
 * @author yduchesne
 * 
 */
public class HttpContext {

  private HttpRequestFacade request;
  private HttpResponseFacade response;
  private String contextPath, pathInfo;

  /**
   * @return this instance's {@link HttpRequestFacade}.
   */
  public HttpRequestFacade getRequest() {
    return request;
  }

  public void setRequest(HttpRequestFacade request) {
    this.request = request;
  }

  /**
   * @return this instance's {@link HttpResponseFacade} object.
   */
  public HttpResponseFacade getResponse() {
    return response;
  }

  public void setResponse(HttpResponseFacade response) {
    this.response = response;
  }

  /**
   * @return the context path of the request to which this instance corresponds.
   */
  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  /**
   * @return the path info of the request to which this instance corresponds.
   */
  public String getPathInfo() {
    return pathInfo;
  }

  public void setPathInfo(String pathInfo) {
    this.pathInfo = pathInfo;
  }
}
