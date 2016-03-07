package org.sapia.corus.cloud.platform.http;

import java.util.HashMap;
import java.util.Map;

public class Headers {

  private Map<String, String> map = new HashMap<>();
  
  public Headers add(String name, String value) {
    map.put(name, value);
    return this;
  }
  
  public Map<String, String> asMap() {
    return map;
  }
  
  public static Headers create() {
    return new Headers();
  }
  
  public static Headers of(String...nameValues) {
    Headers headers = new Headers();
    for (int i = 0; i <= nameValues.length; i+=2) {
      if (i + 1 < nameValues.length) {
        headers.map.put(nameValues[i], nameValues[i + 1]);
      }
    }
    return headers;
  }
}
