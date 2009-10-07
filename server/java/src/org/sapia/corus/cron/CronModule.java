package org.sapia.corus.cron;

import java.util.List;

import org.sapia.corus.admin.Module;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.exceptions.LogicException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface CronModule extends java.rmi.Remote, Module {
  public static final String ROLE = CronModule.class.getName();

  public void addCronJob(CronJobInfo info)
                  throws InvalidTimeException, LogicException, CorusException;

  public void removeCronJob(String id);

  public List listCronJobs();
}
