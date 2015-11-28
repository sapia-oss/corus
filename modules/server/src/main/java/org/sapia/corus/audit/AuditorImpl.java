package org.sapia.corus.audit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Assertions;

/**
 * Implementation of the {@link Auditor} interface.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = { Auditor.class })
public class AuditorImpl extends ModuleHelper implements Auditor {

  private static final String AUDIT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss:SSS";
  private static final String LINE_SEPARATOR     = System.getProperty("line.separator");
  
  private static ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>();

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(Auditor.ROLE);
  
  @Override
  public String getRoleName() {
    return Auditor.ROLE;
  }
  
  @Override
  public void audit(AuditInfo info, ServerAddress remoteAddr, String moduleName, String methodName) {
    Assertions.illegalState(info.isEncrypted(), "Expected AuditInfo to be decrypted at this point");
    DateFormat dtf = getDateFormat();
    if (remoteAddr instanceof TCPAddress) {
      TCPAddress tcpAddress = (TCPAddress) remoteAddr;
      log.debug(ToStringUtils.joinAsCsv(
          dtf.format(new Date()), 
          info.getRequestId(), 
          tcpAddress.getHost(), 
          info.getUserToken(), 
          info.getType().name().toLowerCase(), 
          moduleName, 
          methodName
      ) + LINE_SEPARATOR);
    } else {
      log.debug(ToStringUtils.joinAsCsv(
          dtf.format(new Date()), 
          info.getRequestId(), 
          remoteAddr, 
          info.getUserToken(), 
          info.getType().name().toLowerCase(), 
          moduleName, 
          methodName
      ) + LINE_SEPARATOR);
    }
  }
  
  @Override
  public void init() throws Exception {
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  private DateFormat getDateFormat() {
    DateFormat toReturn = DATE_FORMAT.get();
    if (toReturn == null) {
      toReturn = new SimpleDateFormat(AUDIT_DATE_PATTERN);
      DATE_FORMAT.set(toReturn);
    }
    return toReturn;
  }
}
