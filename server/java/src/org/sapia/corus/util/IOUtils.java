package org.sapia.corus.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * I/O utilisty methods.
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class IOUtils {
  
  /**
   * Extracts all the current available data of the passed in input stream and add it
   * to the output stream. When no data is available, it closes the input stream and
   * flush the data of the output stream.
   * 
   * @param anInput The input stream from which to read the data.
   * @param anOutput The output stream to which to write the data.
   * @throws IOException If an error occurs extracting the data.
   */
  public static void extractAvailable(InputStream anInput, OutputStream anOutput) throws IOException {
    InputStream is = new BufferedInputStream(anInput);
    try {
      byte[] someData = new byte[1024];
      int length = 0;
      int size = is.available();
      while (size > 0) {
        if (size > someData.length) {
          length = is.read(someData, 0, someData.length);
        } else {
          length = is.read(someData, 0, size);
        }
        anOutput.write(someData, 0, length);
        size = is.available();
      } 
    } finally {
      try {
        if (anOutput != null) anOutput.flush();
        if (is != null) is.close();
      } catch (IOException ioe) {
      }
    }
  }
  
  /**
   * Extracts the available data of the passed in input stream and add it to the output
   * stream. If no current data is available, it will wait up to the timeout value passed
   * in for data. When all the available data is retrieved or when no data is available and
   * the timeout value is reached, it closes the input stream and flush the data of the
   * output stream.
   * 
   * @param anInput The input stream from which to read the data.
   * @param anOutput The output stream to which to write the data.
   * @param aTimeout The timeout value to stop wainting on available data.
   * @throws IOException If an error occurs extracting the data.
   */
  public static void extractUntilAvailable(InputStream anInput, OutputStream anOutput, int aTimeout) throws IOException {
    boolean hasRead = false;
    long aStart = System.currentTimeMillis();
    InputStream is = new BufferedInputStream(anInput);

    try {
      byte[] someData = new byte[1024];
      while (!hasRead) {
        int length = 0;
        int size = is.available();
        while (size > 0) {
          if (size > someData.length) {
            length = is.read(someData, 0, someData.length);
          } else {
            length = is.read(someData, 0, size);
          }
          hasRead = true;
          anOutput.write(someData, 0, length);
          size = is.available();
        } 
        
        if (!hasRead && ((System.currentTimeMillis() - aStart) <= aTimeout)) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException ie) {
          }
        } else {
          break;
        }
      } 
    } finally {
      try {
        if (anOutput != null) anOutput.flush();
        if (is != null) is.close();
      } catch (IOException ioe) {
      }
    }
  }
}
