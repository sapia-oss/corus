package org.sapia.corus.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.alert.AlertManager;
import org.sapia.corus.client.services.deployer.event.DeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Status;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent.Type;
import org.sapia.corus.client.services.deployer.event.UndeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentFailedEvent;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.client.services.processor.event.ProcessStaleEvent;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Strings;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Implements alert management. An instance of this class sends email alerts
 * corresponding to the following events:
 * <ul>
 * <li> {@link DeploymentCompletedEvent}
 * <li> {@link DeploymentFailedEvent}
 * <li> {@link UndeploymentCompletedEvent}
 * <li> {@link UndeploymentFailedEvent}
 * <li> {@link ProcessKilledEvent}
 * <li> {@link RollbackCompletedEvent}
 * </ul>
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = AlertManager.class)
public class AlertManagerImpl extends ModuleHelper implements AlertManager, Interceptor {

  private static final int    ALERT_SENDERS     = 3;
  private static final int    DEFAULT_SMTP_PORT = 25;
  private static final String DEFAULT_SMTP_HOST = "localhost";

  private String        smtpHost = DEFAULT_SMTP_HOST;
  private int           smtpPort = DEFAULT_SMTP_PORT;
  private String        smtpPassword;
  private List<String>  recipientList;
  private String        sender;
  private boolean       enabled;
  private MailSender    mailSender;
  private AlertLevel    minLevel = AlertLevel.WARNING;

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
  
  public void setAlertLevel(String levelName) {
    this.minLevel = AlertLevel.forName(levelName);
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
    super.serverContext.getServices().getEventDispatcher().addInterceptor(DeploymentCompletedEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(DeploymentFailedEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(UndeploymentCompletedEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(UndeploymentFailedEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(RollbackCompletedEvent.class, this);
    super.serverContext.getServices().getEventDispatcher().addInterceptor(ProcessStaleEvent.class, this);
    
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

  public void onProcessStaleEvent(final ProcessStaleEvent event) {

    if (event.getProcess().getStaleDetectionCount() == 1) {
      alertSenders.execute(new Runnable() {
        @Override
        public void run() {
          sendAlert(
              AlertLevel.WARNING,
              subject(AlertLevel.WARNING, "Process is stale"),
              AlertBuilder
                  .newInstance()
                  .serverContext(serverContext())
                  .level(AlertLevel.WARNING)
                  .summary("Process is stale")
                  .details(
                      "Process " + event.getProcess().getProcessID() + " has been detected as stale by the Corus server "
                          + "(the process has not been restarted since auto-restart is disabled)")
                  .field("Distribution", event.getProcess().getDistributionInfo().getName())
                  .field("Version", event.getProcess().getDistributionInfo().getVersion())
                  .field("Process name", event.getProcess().getDistributionInfo().getProcessName())
                  .field("Profile", event.getProcess().getDistributionInfo().getProfile()).build());
        }
      });
    }
  }

  // --------------------------------------------------------------------------
  // Process kill
  
  public void onProcessKilledEvent(final ProcessKilledEvent event) {
    if (event.getRequestor() == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (event.wasRestarted()) {
        alertSenders.execute(new Runnable() {
          @Override
          public void run() {
            sendAlert(
                AlertLevel.WARNING,
                subject(AlertLevel.WARNING, "Process restarted"),
                AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.WARNING).summary("Process restarted")
                    .details("Process " + event.getProcess().getProcessID() + " was restarted by the Corus server")
                    .field("Distribution", event.getProcess().getDistributionInfo().getName())
                    .field("Version", event.getProcess().getDistributionInfo().getVersion())
                    .field("Process name", event.getProcess().getDistributionInfo().getProcessName())
                    .field("Profile", event.getProcess().getDistributionInfo().getProfile()).build());
          }
        });
      } else {
        alertSenders.execute(new Runnable() {
          @Override
          public void run() {
            sendAlert(
                AlertLevel.ERROR,
                subject(AlertLevel.ERROR, "Process terminated"),
                AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.ERROR).summary("Process terminated")
                    .details("Process " + event.getProcess().getProcessID() + " was terminated by the Corus server")
                    .field("Distribution", event.getProcess().getDistributionInfo().getName())
                    .field("Version", event.getProcess().getDistributionInfo().getVersion())
                    .field("Process name", event.getProcess().getDistributionInfo().getProcessName())
                    .field("Profile", event.getProcess().getDistributionInfo().getProfile()).build());
          }
        });
      }
    }

  }
  
  // --------------------------------------------------------------------------
  // Deployment

  public void onDeploymentCompletedEvent(final DeploymentCompletedEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        sendAlert(
            AlertLevel.INFO,
            subject(AlertLevel.INFO, "Distribution deployed"),
            AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.INFO).summary("Distribution deployed")
                .field("Distribution", event.getDistribution().getName()).field("Version", event.getDistribution().getVersion()).build());
      }
    });
  }
  
  public void onDeploymentFailedEvent(final DeploymentFailedEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        AlertBuilder builder = AlertBuilder.newInstance();
        if (event.getDistribution().isSet()) {
          builder
            .serverContext(serverContext())
            .level(AlertLevel.ERROR).summary("Distribution deployment failed")
            .field("Distribution", event.getDistribution().get().getName())
            .field("Version", event.getDistribution().get().getVersion());
        } else {
          builder
            .serverContext(serverContext())
            .level(AlertLevel.ERROR).summary("Distribution deployment failed - could not be unpacked");
        }
        sendAlert(AlertLevel.ERROR, subject(AlertLevel.ERROR, "Distribution deployment failed"), builder.build());
      }
    });
  }
  
  // --------------------------------------------------------------------------
  // Rollback
  
  public void onRollbackCompletedEvent(final RollbackCompletedEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        
        if (event.getStatus() == Status.SUCCESS) {
          if (event.getType() == Type.AUTO) {
            sendAlert(
                AlertLevel.ERROR,
                subject(AlertLevel.ERROR, "Distribution rolled back automatically by Corus"),
                AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.ERROR)
                    .summary("Distribution rolled back")
                    .field("Distribution", event.getDistribution().getName())
                    .field("Version", event.getDistribution().getVersion())
                    .field("Type", event.getType().name())
                    .field("Status", event.getStatus().name())
                    .build()
            );
          } else {
            sendAlert(
                AlertLevel.WARNING,
                subject(AlertLevel.WARNING, "Distribution rolled back by user"),
                AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.WARNING)
                    .summary("Distribution rolled back")
                    .field("Distribution", event.getDistribution().getName())
                    .field("Version", event.getDistribution().getVersion())
                    .field("Type", event.getType().name())
                    .field("Status", event.getStatus().name())
                    .build()
            );            
          }
        } else {
          sendAlert(
              AlertLevel.ERROR,
              subject(AlertLevel.ERROR, "Distribution rollback failed"),
              AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.ERROR)
                  .summary("Distribution rolled back")
                  .field("Distribution", event.getDistribution().getName())
                  .field("Version", event.getDistribution().getVersion())
                  .field("Type", event.getType().name())
                  .field("Status", event.getStatus().name())
                  .build()
          );            
        }
      }
    });
  }
  
  // --------------------------------------------------------------------------
  // Undeployment

  public void onUndeploymentCompletedEvent(final UndeploymentCompletedEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        sendAlert(
            AlertLevel.INFO,
            subject(AlertLevel.INFO, "Distribution undeployed"),
            AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.INFO).summary("Distribution undeployed")
                .field("Distribution", event.getDistribution().getName()).field("Version", event.getDistribution().getVersion()).build());
      }
    });
  }
  
  public void onUndeploymentFailedEvent(final UndeploymentFailedEvent event) {
    alertSenders.execute(new Runnable() {
      @Override
      public void run() {
        sendAlert(
            AlertLevel.ERROR,
            subject(AlertLevel.ERROR, "Distribution undeployment failed"),
            AlertBuilder.newInstance().serverContext(serverContext()).level(AlertLevel.ERROR).summary("Distribution undeployment failed")
                .field("Distribution", event.getDistribution().getName()).field("Version", event.getDistribution().getVersion()).build());
      }
    });
  }

  // --------------------------------------------------------------------------
  // Restricted methods
  
  void sendAlert(AlertLevel level, String subject, String content) {
    if (minLevel.isEnabled(level) && enabled && !Strings.isBlank(smtpHost) && !recipientList.isEmpty()) {
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
