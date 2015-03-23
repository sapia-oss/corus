package org.sapia.corus.client.services.processor;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * Used to match processes based on their port.
 * 
 * @author yduchesne
 *
 */
public class PortCriteria {

  public static class Builder {
    
    private ArgMatcher range;
    
    private ArgMatcher port;
    
    private Builder() {
    }
    
    public Builder range(String range) {
      this.range = ArgMatchers.exact(range);
      return this;
    }
    
    public Builder port(int port) {
      this.port = ArgMatchers.exact(Integer.toString(port));
      return this;
    }
    
    public Builder range(ArgMatcher range) {
      this.range = range;
      return this;
    }
    
    public Builder port(ArgMatcher port) {
      this.port = port;
      return this;
    }
    
    public PortCriteria all() {
      range = ArgMatchers.any();
      port  = ArgMatchers.any();
      return new PortCriteria(range, port);
    }
    
    public PortCriteria build() {
      range = ArgMatchers.anyIfNull(range);
      port  = ArgMatchers.anyIfNull(port);
      return new PortCriteria(range, port);
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private ArgMatcher range;
  private ArgMatcher port;
 
  /**
   * Meant for externalization only.
   */
  public PortCriteria() {
  }
  
  private PortCriteria(ArgMatcher range, ArgMatcher port) {
    this.range = range;
    this.port  = port;
  }
  
  /**
   * @return the {@link ArgMatcher} used to match against port values.
   */
  public ArgMatcher getPort() {
    return port;
  }
  
  /**
   * @return the {@link ArgMatcher} used to match against port range names.
   */
  public ArgMatcher getRange() {
    return range;
  }
  
  /**
   * @return a new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }
  
  /**
   * @param literal a port range literal, of the form: <code>range_name</code>:<code>port_value</code>
   * @return the {@link PortCriteria} corresponding to the given literal.
   */
  public static PortCriteria fromLiteral(String literal) {
    String[] parts = StringUtils.split(literal, ':');
    if (parts.length == 0) {
      return new PortCriteria(ArgMatchers.any(), ArgMatchers.any());
    } else if (parts.length == 1) {
      return new PortCriteria(ArgMatchers.parse(parts[0]), ArgMatchers.any());
    } else {
      return new PortCriteria(ArgMatchers.parse(parts[0]), ArgMatchers.parse(parts[1]));
    }
  }
  
}
