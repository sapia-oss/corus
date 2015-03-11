package org.sapia.corus.client.services.database;

/**
 * Indicates if an archival operation overwrote a previous revision, or created a new one.
 * 
 * @author yduchesne
 *
 */
public enum ArchiveStatus {
  CREATE,
  OVERWRITE;
}