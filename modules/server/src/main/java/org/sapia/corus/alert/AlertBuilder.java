package org.sapia.corus.alert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.sapia.corus.client.services.alert.AlertManager.AlertLevel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Assertions;

/**
 * Builds the content of an alert.
 * 
 * @author yduchesne
 *
 */
public class AlertBuilder {
  
  static class Field {
    
    private String name;
    private String value;
     
    Field(String name, String value) {
      this.name  = name;
      this.value = value;
    }
    
    String getName() {
      return name;
    }
    
    String getValue() {
      return value;
    }
  }
  
  // --------------------------------------------------------------------------

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss z");
  
  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  
  private AlertLevel    level;
  private String        summary;
  private String        details;
  private String        corusHost;
  private List<Field>   fields = new ArrayList<Field>();
  
  private AlertBuilder() {
  }
  
  public AlertBuilder corusHost(ServerAddress addr) {
    if (addr instanceof TCPAddress) {
      TCPAddress tcpAddr = (TCPAddress) addr;
      corusHost = ((TCPAddress) addr).getHost() + ":" + tcpAddr.getPort();
    } else {
      corusHost = addr.toString();
    }
    return this;
  }
  
  public AlertBuilder level(AlertLevel level) {
    this.level = level;
    return this;
  }
  
  public AlertLevel getLevel() {
    return level;
  }
  
  public AlertBuilder summary(String summary) {
    this.summary = summary;
    return this;
  }
  
  public AlertBuilder details(String details) {
    this.details = details;
    return this;
  }
  
  
  public AlertBuilder field(String name, String value) {
    if (value != null) {
      this.fields.add(new Field(name, value));
    }
    return this;
  }
  
  public String build() {
    Assertions.notNull(corusHost, "Corus host not specified");
    Assertions.notNull(level, "Level not specified");
    Assertions.notNull(summary, "Summary not specified");
    
    StringBuilder builder = new StringBuilder();
    builder.append("LEVEL: ").append(level.name()).append("\r\n");
    builder.append("SUMMARY: ").append(summary).append("\r\n");
    builder.append("HOST: ").append(corusHost).append("\r\n");
    builder.append("TIME: ").append(DATE_FORMAT.format(new Date())).append("\r\n");    
    for (Field f : fields) {
      builder.append(f.getName().toUpperCase()).append(": ").append(f.getValue()).append("\r\n");
    }
    if (details != null) {
      builder.append("\r\n").append(details).append("\r\n");
    }
    return builder.toString();
  }
  
  public static AlertBuilder newInstance() {
    return new AlertBuilder();
  }
  
  public static void main(String[] args) {
    System.out.println(DATE_FORMAT.format(new Date()));
  }

}

