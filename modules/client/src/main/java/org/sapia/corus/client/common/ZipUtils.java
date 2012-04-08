package org.sapia.corus.client.common;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Provides utility methods pertaining to zip file manipulation.
 * 
 * @author Yanick Duchesne
 */
public class ZipUtils {
  /**
   * Reads an entry from a jar file (a {@link ZipInputStream})
   * and returns it as an array of bytes.
   *
   * @param is the {@link ZipInputStream} containing the entry.
   * @param name the entry's name.
   * @param capacity the maximum buffer size of the internal byte array
   * used to read the bytes.
   * @param increment the size by which the receive array of bytes should
   * be increased when its end has been reached.
   *
   * @return a <code>byte</code> array - will be of length 0 if no entry
   * could be found for the given name.
   *
   * @throws IOException if a problem occurs reading the entry.
   */
  public static byte[] readEntry(ZipInputStream is, String name, int capacity,
                                 int increment) throws IOException {
    ZipEntry entry = null;

    while ((is.available() > 0) && ((entry = is.getNextEntry()) != null)) {
      if (entry.getName().equals(name)) {
        return readBytes(is, capacity, increment);
      }
    }

    throw new IOException("Not entry in JAR for: " + name);

    //    return new byte[0];
  }

  /**
   * Reads an entry from a jar file and returns it as a stream.
   *
   * @param fileName the name of the jar file to read the entry from.
   * @param name the name of the entry to look for.
   * @param capacity the maximum buffer size of the internal byte array
   * used to read the bytes.
   * @param increment the size by which the receive array of bytes should
   * be increased when its end has been reached.
   *
   * @throws IOException if a problem occurs reading the entry.
   */
  public static InputStream readEntryStream(String fileName, String name,
                                            int capacity, int increment)
                                     throws IOException {
    ZipInputStream is = null;

    try {
      is = new ZipInputStream(new FileInputStream(fileName));

      return readEntryStream(is, name, capacity, increment);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Throwable t) {
          //noop;
        }
      }
    }
  }

  /**
   * Reads an entry from a jar file (as a {@link ZipInputStream}) and returns
   * it as a stream.
   *
   * @param is the {@link ZipInputStream} containing the entry.
   * @param name the name of the entry to look for.
   * @param capacity the maximum buffer size of the internal byte array
   * used to read the bytes.
   * @param increment the size by which the receive array of bytes should
   * be increased when its end has been reached.
   *
   * @throws IOException if a problem occurs reading the entry.
   */
  public static InputStream readEntryStream(ZipInputStream is, String name,
                                            int capacity, int increment)
                                     throws IOException {
    return new ByteArrayInputStream(readEntry(is, name, capacity, increment));
  }

  static byte[] readBytes(ZipInputStream is, int capacity, int increment)
                   throws IOException {
    //ZipEntry  entry;
    ArrayList<ByteWrapper> entryBytes = new ArrayList<ByteWrapper>(capacity);
    byte[]    current = new byte[increment];

    int       currentRead = 0;

    while (is.available() > 0) {
      currentRead = is.read(current, 0, increment);

      if (currentRead == -1) {
        break;
      }

      for (int i = 0; i < currentRead; i++) {
        entryBytes.add(new ByteWrapper(current[i]));
      }
    }

    byte[] toReturn = new byte[entryBytes.size()];

    for (int i = 0; i < entryBytes.size(); i++) {
      toReturn[i] = ((ByteWrapper) entryBytes.get(i)).theByte;
    }

    return toReturn;
  }

  static class ByteWrapper {
    byte theByte;

    ByteWrapper(byte b) {
      theByte = b;
    }
  }
}
