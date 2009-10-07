package org.sapia.corus.db;

import java.io.IOException;

import jdbm.JDBMRecordManager;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JdbmDb {
  private JDBMRecordManager _recman;

  /**
   * Constructor for JispDb.
   */
  private JdbmDb(JDBMRecordManager recman) {
    _recman = recman;
  }

  DbMap getDbMap(String name) throws IOException {
    return new DbMapImpl(_recman.getHashtable(name));
  }

  static JdbmDb open(String fName) throws IOException {
    return new JdbmDb(new JDBMRecordManager(fName));
  }

  void close() {
    try {
      _recman.close();
    } catch (IOException e) {
      // noop;
    }
  }

  public static void main(String[] args) {
    try {
      JdbmDb db = JdbmDb.open("test");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
