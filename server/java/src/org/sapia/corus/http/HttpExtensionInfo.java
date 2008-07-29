package org.sapia.corus.http;

public class HttpExtensionInfo implements Comparable{
  
  private String name, description, contextPath;

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public int hashCode() {
    return contextPath.hashCode();
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof HttpExtensionInfo){
      HttpExtensionInfo other = (HttpExtensionInfo)obj;
      return contextPath.equals(other.contextPath);
    }
    else{
      return false;
    }
  }
  
  public int compareTo(Object obj) {
    HttpExtensionInfo other = (HttpExtensionInfo)obj;
    return name.compareTo(other.getName());
  }

}
