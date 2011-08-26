package org.sapia.corus.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;


/**
 * I/O utility methods.
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class IOUtils {
  
  /**
   * Extracts all the available data of the passed in input stream and add it
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

  /**
   * Performs variable substitution (with variables having form <code>${variable}</code>) in the
   * content of the given stream.
   * 
   * @param lookup a {@link StrLookup} instance used to look up variable values.
   * @param textStream the {@link InputStream} containing the text in which to perform
   * variable substitution.
   * @return the {@link InputStream} holding the text with the substituted content.
   * @throws IOException if an IO problem occurs while performing substitution.
   */
  public static InputStream replaceVars(StrLookup lookup, InputStream textStream) throws IOException{
    String text = textStreamToString(textStream);
    StrSubstitutor stb = new StrSubstitutor(lookup);
    text = stb.replace(text);
    return new ByteArrayInputStream(text.getBytes());
  }
  
  /**
   * Returns the content in the given stream in the form of a string.
   * <p>
   * Note: the stream is closed by this method.
   * 
   * @param is an {@link InputStream}
   * @return a {@link String} containing the data in the given steam. 
   * @throws IOException if an IO problem occurs.
   */
  public static String textStreamToString(InputStream is) throws IOException{
    try{
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      String lineSep = System.getProperty("line.separator");

      StringBuilder content = new StringBuilder();
      while((line = reader.readLine()) != null){
        content.append(line).append(lineSep);
      }
      
      return content.toString();
      
    }finally{
      try {
        is.close();
      } catch (IOException e) {
      }
    }
  }
 

  /**
   * @param file the {@link File} for which a lock file should be created.
   * @throws IOException if a corresponding lock file already exists or could not be created.
   */
  public static void createLockFile(File file) throws IOException{
    if(!file.createNewFile()){
      throw new IOException(String.format("Lock file already exists, server probably running %s", file.getAbsolutePath()));
    }
    file.deleteOnExit();
  }
  
}
