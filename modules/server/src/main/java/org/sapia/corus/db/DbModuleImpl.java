package org.sapia.corus.db;

import java.io.File;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.exceptions.core.IORuntimeException;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.core.ModuleHelper;

/**
 * This class implements the {@link DbModule} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = DbModule.class)
public class DbModuleImpl extends ModuleHelper implements DbModule {

  /**
   * The {@link File} corresponding to the directory where database files are
   * kept.
   */
  private File dbDir;

  /**
   * The {@link PersistentDb} instance.
   */
  private PersistentDb db;

  public DbModuleImpl() {
    super();
  }

  public void setDbDir(String dbDirPath) {
    this.dbDir = new File(dbDirPath);
  }

  @Override
  public void init() throws Exception {
    if (dbDir != null) {
      String aFilename = new StringBuffer(dbDir.getAbsolutePath())
            .append(File.separator)
            .append(serverContext().getNodeSubdirName())
            .toString();
      dbDir = new File(aFilename);

    } else {
      String aFilename = new StringBuffer(serverContext().getHomeDir())
          .append(File.separator).append("db").append(File.separator)
          .append(serverContext().getNodeSubdirName())
          .toString();
      dbDir = new File(aFilename);
    }

    logger().debug(String.format("DB module directory %s", dbDir.getAbsolutePath()));

    if (!dbDir.exists()) {
      if (!dbDir.mkdirs()) {
        throw new IllegalStateException("Could not make directory: " + dbDir.getAbsolutePath());
      }
    }

    try {
      db = PersistentDb.open(dbDir.getAbsolutePath() + File.separator + File.separator + "database");
    } catch (Exception e) {
      throw new IllegalStateException("Could not open database", e);
    }
  }

  public void dispose() {
    if (db != null) {
      try {
        db.close();
      } catch (RuntimeException e) {
      }
    }
  }

  /*
   * //////////////////////////////////////////////////////////////////// Module
   * INTERFACE METHOD
   * ////////////////////////////////////////////////////////////////////
   */

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return DbModule.ROLE;
  }

  /*
   * ////////////////////////////////////////////////////////////////////
   * DbModule INTERFACE METHODS
   * ////////////////////////////////////////////////////////////////////
   */

  @Override
  public <K, V> DbMap<K, V> getDbMap(Class<K> keyType, Class<V> valueType, String name) {
    try {
      return db.getDbMap(keyType, valueType, name);
    } catch (java.io.IOException e) {
      throw new IORuntimeException(e);
    }
  }
}
