package org.sapia.corus.client.services.http;

/**
 * Encapsulates information about a {@link HttpExtension}.
 * 
 * @author yduchesne
 * 
 */
public class HttpExtensionInfo implements Comparable<HttpExtensionInfo> {

  private String name, description, contextPath;

  /**
   * @return the extension's context path.
   */
  public String getContextPath() {
    return contextPath;
  }

  public HttpExtensionInfo setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  /**
   * @return the extension's description.
   */
  public String getDescription() {
    return description;
  }

  public HttpExtensionInfo setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * @return the extension's name.
   */
  public String getName() {
    return name;
  }

  public HttpExtensionInfo setName(String name) {
    this.name = name;
    return this;
  }
  
  /**
   * Convenience factory method, creates instances of this class.
   * @return a new {@link HttpExtensionInfo}.
   */
  public static HttpExtensionInfo newInstance() {
    return new HttpExtensionInfo();
  }

  public int hashCode() {
    return contextPath.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof HttpExtensionInfo) {
      HttpExtensionInfo other = (HttpExtensionInfo) obj;
      return contextPath.equals(other.contextPath);
    } else {
      return false;
    }
  }

  public int compareTo(HttpExtensionInfo other) {
    return name.compareTo(other.getName());
  }

}
