package org.sapia.corus.db;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.ModuleHelper;
import org.sapia.ubik.net.TCPAddress;

import java.io.File;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DbModuleImpl extends ModuleHelper implements DbModule{
  private File   _dbDir;
  private JdbmDb _db;

  /**
   * Constructor for DbModuleImpl.
   */
  public DbModuleImpl() {
    super();
  }
  
  public void setDbDir(String dbDir){
    _dbDir = new File(dbDir);
  }

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    if (_dbDir != null) {
      String aFilename = new StringBuffer(_dbDir.getAbsolutePath()).
              append(File.separator).append(CorusRuntime.getCorus().getDomain()).
              append("_").append(((TCPAddress)CorusRuntime.getTransport().getServerAddress()).getPort()).
              toString();
      _dbDir = new File(aFilename);

    } else {
      String aFilename = new StringBuffer(CorusRuntime.getCorusHome()).
              append(File.separator).append("db").
              append(File.separator).append(CorusRuntime.getCorus().getDomain()).
              append("_").append(((TCPAddress)CorusRuntime.getTransport().getServerAddress()).getPort()).
              toString();
      _dbDir = new File(aFilename);
    }
    
    if (!_dbDir.exists()) {
      if (!_dbDir.mkdirs()) {
        throw new IllegalStateException("Could not make directory: " + _dbDir.getAbsolutePath());
      }
    }

    _db = JdbmDb.open(_dbDir.getAbsolutePath() + File.separator + File.separator + "database");
  }

  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    if (_db != null) {
      try{
        _db.close();
      }catch(RuntimeException e){}
    }
  }

  /*////////////////////////////////////////////////////////////////////
                        Module INTERFACE METHOD
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return DbModule.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                        DbModule INTERFACE METHODS  
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.db.DbModule#getDbMap(String)
   */
  public DbMap getDbMap(String name) {
    try {
      return new CacheDbMap(_db.getDbMap(name));
    } catch (java.io.IOException e) {
      throw new IORuntimeException(e);
    }
  }
}
