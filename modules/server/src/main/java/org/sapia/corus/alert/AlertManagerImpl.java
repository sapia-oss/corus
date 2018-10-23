package org.sapia.corus.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.alert.AlertManager;
import org.sapia.corus.client.services.event.CorusEvent;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Strings;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Implements alert management. An instance of this class sends email alerts
 * corresponding to events having a level corresponding to a least the level
 * configured as threshold:
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = AlertManager.class)
public class AlertManagerImpl extends ModuleHelper implements AlertManager {

  private static final int    DEFAULT_SMTP_PORT = 25;
  private static final String DEFAULT_SMTP_HOST = "localhost";

  private String        smtpHost = DEFAULT_SMTP_HOST;
  private int           smtpPort = DEFAULT_SMTP_PORT;
  private String        smtpPassword;
  private List<String>  recipientList;
  private String        sender;
  private boolean       enabled;
  private MailSender    mailSender;
  private EventLevel    minLevel = EventLevel.WARNING;

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
    this.minLevel = EventLevel.forName(levelName);
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
    serverContext().getServices().getEventDispatcher().addInterceptor(CorusEvent.class, this);
    
    alertSenders = Threads.createIoOutboundPool();
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

  public void onCorusEvent(final CorusEvent event) {
    if (event.getLevel().isAtLeast(minLevel) && enabled && !Strings.isBlank(smtpHost) && !recipientList.isEmpty()) {
      alertSenders.submit(() -> {
        sendAlert(event);
      });
    }
  } 
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  void sendAlert(CorusEvent event) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      if (!Strings.isBlank(sender)) {
        message.setFrom(sender);
      } else {
        message.setFrom("corus-noreply@" + smtpHost);
      }
      EventLog eventLog = event.toEventLog();
      message.setSubject("[" + serverContext().getDomain() + "] " + eventLog.getType());
      message.setText(eventLog.getMessage());
      message.setTo(recipientList.toArray(new String[recipientList.size()]));
      mailSender.send(message);
    } catch (RuntimeException e) {
      log.error("Could not send alert", e);
    }
  }

}
