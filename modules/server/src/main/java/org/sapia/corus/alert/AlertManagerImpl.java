package org.sapia.corus.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.alert.AlertManager;
import org.sapia.corus.client.services.deployer.event.DeploymentEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentEvent;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Strings;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Implements alert management. An instance of this class sends email alerts corresponding to the following
 * events:
 * <ul>
 *   <li> {@link DeploymentEvent}
 *   <li> {@link UndeploymentEvent}
 *   <li> {@link ProcessKilledEvent}
 * </ul>
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = AlertManager.class)
public class AlertManagerImpl extends ModuleHelper implements AlertManager, Interceptor {
  
  private static final int ALERT_SENDERS        = 3;
  private static final int DEFAULT_SMTP_PORT    = 25;
  private static final String DEFAULT_SMTP_HOST = "localhost";
  
  private String       smtpHost        = DEFAULT_SMTP_HOST;
  private int          smtpPort        = DEFAULT_SMTP_PORT;
  private String       smtpPassword;
  private List<String> recipientList;
  private String       sender;
  private boolean      enabled;
  private MailSender   mailSender;
  
  private ExecutorService alertSenders;
  
  public void setSmtpHost(String smtpHost) {
    if (!Strings.isBlank(smtpHost)) {
      this.smtpHost = smtpHost;
    }
  }
  
  public void setFromEmail(String from) {
    if (!Strings.isBlank(from)) {
      this.sender = from;
    }
  }
  
  public void setSmtpPort(String smtpPort) {
    if (!Strings.isBlank(smtpPort)) {
      this.smtpPort = Integer.parseInt(smtpPort);
    }
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public void setSmtpPassword(String smtpPassword) {
    if (!Strings.isBlank(smtpPassword)) {
      this.smtpPassword = smtpPassword;
    }
  }
  
  public void setRecipientEmails(String recipientEmailCsv) {
    recipientList = new ArrayList<String>();
    String[] recipientEmails = recipientEmailCsv.split(";");
    for (String recipient : recipientEmails) {
      recipient = recipient.trim();
      if (!recipient.isEmpty()) {
        recipientList.add(recipient);
      }
    }
  }
  
  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    super.serverContext.getServices().getEventDispatcher().addInterceptor(ProcessKilledEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(DeploymentEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(UndeploymentEvent.class, this);
    alertSenders = Executors.newFixedThreadPool(ALERT_SENDERS);
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost(this.smtpHost);
    if (!Strings.isBlank(smtpPassword)) {
      javaMailSender.setPassword(smtpPassword);
    }
    javaMailSender.setPort(smtpPort);
    mailSender = javaMailSender;
    
    if (log.isDebugEnabled()) {
      log.debug("Alert recipients: " + recipientList);
      log.debug("Alerting enabled: " + enabled);
    }
    
  }
  
  @Override
  public void dispose() throws Exception {
    alertSenders.shutdown();
  }
  
  // --------------------------------------------------------------------------
  // interception methods
  
  public void onProcessKilledEvent(final ProcessKilledEvent event) {
    if (event.getRequestor() == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (event.wasRestarted()) {
        alertSenders.execute(new Runnable() {
          @Override
          public void run() {
            sendAlert(
                subject(AlertLevel.WARNING, "Process restarted"),
                AlertBuilder.newInstance()
                  .serverContext(serverContext())                
                  .level(AlertLevel.WARNING)
                  .summary("Process restarted")
                  .details("Process " + event.getProcess().getProcessID() + " was restarted by the Corus server")
                  .field("Distribution", event.getProcess().getDistributionInfo().getName())
                  .field("Version", event.getProcess().getDistributionInfo().getVersion())
                  .field("Process name", event.getProcess().getDistributionInfo().getProcessName())
                  .field("Profile", event.getProcess().getDistributionInfo().getProfile())
                  .build());
          }
        });
      } else {
        alertSenders.execute(new Runnable() {
          @Override
          public void run() {
            sendAlert(
                subject(AlertLevel.ERROR, "Process terminated"),            
                AlertBuilder.newInstance()
                  .serverContext(serverContext())
                  .level(AlertLevel.ERROR)
                  .summary("Process terminated")
                  .details("Process " + event.getProcess().getProcessID() + " was terminated by the Corus server")
                  .field("Distribution", event.getProcess().getDistributionInfo().getName())
                  .field("Version", event.getProcess().getDistributionInfo().getVersion())
                  .field("Process name", event.getProcess().getDistributionInfo().getProcessName())
                  .field("Profile", event.getProcess().getDistributionInfo().getProfile())
                  .build());        
          }
        });
      }
    }
    
  }
  
  public void onDeploymentEvent(final DeploymentEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        sendAlert(
            subject(AlertLevel.INFO, "Distribution deployed"),
            AlertBuilder.newInstance()
              .serverContext(serverContext())            
              .level(AlertLevel.INFO)
              .summary("Distribution deployed")
              .field("Distribution", event.getDistribution().getName())
              .field("Version", event.getDistribution().getVersion())
              .build());
      }
    });
  }  
  
  public void onUndeploymentEvent(final UndeploymentEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        sendAlert(
            subject(AlertLevel.WARNING,"Distribution undeployed"),
            AlertBuilder.newInstance()
              .serverContext(serverContext())
              .level(AlertLevel.WARNING)
              .summary("Distribution undeployed")
              .field("Distribution", event.getDistribution().getName())
              .field("Version", event.getDistribution().getVersion())
              .build());    
      }
    });
  } 
  
  void sendAlert(String subject, String content) {
    if (enabled && !Strings.isBlank(smtpHost) && !recipientList.isEmpty()) {
      log.debug(content);
      try {
        SimpleMailMessage message = new SimpleMailMessage();
        if (!Strings.isBlank(sender)) {
          message.setFrom(sender);
        } else {
          message.setFrom("corus-no-reply@" + smtpHost);          
        }
        message.setSubject("[" + serverContext().getDomain() + "] " + subject);
        message.setText(content);
        message.setTo(recipientList.toArray(new String[recipientList.size()]));
        mailSender.send(message);
      } catch (RuntimeException e) {
        log.error("Could not send alert", e);
      }
    } else {
      log.debug("Alerting disabled, not sending");
    }
  }
  
  private String subject(AlertLevel level, String title) {
    return new StringBuilder().append("[").append(level.name()).append("]").append(title).toString();
  }

}
