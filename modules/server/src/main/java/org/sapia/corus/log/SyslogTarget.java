package org.sapia.corus.log;

import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.format.SyslogFormatter;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

/**
 * A log target that logs to Syslog.
 * 
 * @author yduchesne
 *
 */
public class SyslogTarget implements LogTarget{
  
  public static final String DEFAULT_HOST 		= "localhost";
  public static final int 	 DEFAULT_PORT 		= 514;
  public static final String DEFAULT_PROTOCOL = "udp";
  
  private SyslogIF 				syslog;
  private SyslogFormatter formatter = new SyslogFormatter();
  
  public SyslogTarget(){
    this(DEFAULT_PROTOCOL, DEFAULT_HOST, DEFAULT_PORT);
  }
  
  /**
   * Creates a new instance of this class.
   * 
   * @param protocol the protocol to use (udp, tcp...)
   * @param host the host to which to log
   * @param port the port to which to log.
   */
  public SyslogTarget(String protocol, String host, int port){
    syslog = Syslog.getInstance(protocol);
    syslog.getConfig().setHost(host);
    syslog.getConfig().setPort(port);
    
    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        Syslog.shutdown();
      }
    });
  }
  
  public void processEvent(LogEvent evt) {
    if(evt.getPriority() == Priority.NONE){
      syslog.debug(formatter.format(evt));
    }
    else if(evt.getPriority() == Priority.DEBUG){
      syslog.debug(formatter.format(evt));
    }
    else if(evt.getPriority() == Priority.INFO){
      syslog.info(formatter.format(evt));
    }
    else if(evt.getPriority() == Priority.WARN){
      syslog.warn(formatter.format(evt));
    }
    else if(evt.getPriority() == Priority.ERROR){
      syslog.error(formatter.format(evt));
    }
    else if(evt.getPriority() == Priority.FATAL_ERROR){
      syslog.critical(formatter.format(evt));
    }
  }
  
  

}
