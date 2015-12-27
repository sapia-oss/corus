package org.sapia.corus.client.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;

/**
 * I/O utility methods.
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class IOUtil {

  /**
   * Extracts all the available data of the passed in input stream and add it to
   * the output stream. When no data is available, it closes the input stream
   * and flush the data of the output stream.
   * 
   * @param anInput
   *          The input stream from which to read the data.
   * @param anOutput
   *          The output stream to which to write the data.
   * @throws IOException
   *           If an error occurs extracting the data.
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
        if (anOutput != null)
          anOutput.flush();
        if (is != null)
          is.close();
      } catch (IOException ioe) {
      }
    }
  }

  /**
   * Performs variable substitution (with variables having form
   * <code>${variable}</code>) in the content of the given stream.
   * 
   * @param lookup
   *          a {@link StrLookup} instance used to look up variable values.
   * @param textStream
   *          the {@link InputStream} containing the text in which to perform
   *          variable substitution.
   * @return the {@link InputStream} holding the text with the substituted
   *         content.
   * @throws IOException
   *           if an IO problem occurs while performing substitution.
   */
  public static InputStream replaceVars(StrLookup lookup, InputStream textStream) throws IOException {
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
   * @param is
   *          an {@link InputStream}
   * @return a {@link String} containing the data in the given steam.
   * @throws IOException
   *           if an IO problem occurs.
   */
  public static String textStreamToString(InputStream is) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      String lineSep = System.getProperty("line.separator");

      StringBuilder content = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        content.append(line).append(lineSep);
      }

      return content.toString();

    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  /**
   * Returns the content provided by the given {@link Reader} in the form of a string.
   * <p>
   * Note: the {@link Reader} is closed by this method.
   * 
   * @param toRead
   *          an {@link InputStream}
   * @return a {@link String} containing the data in the given steam.
   * @throws IOException
   *           if an IO problem occurs.
   */
  public static String textReaderToString(Reader toRead) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(toRead);
      String line;
      String lineSep = System.getProperty("line.separator");

      StringBuilder content = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        content.append(line).append(lineSep);
      }

      return content.toString();

    } finally {
      try {
        toRead.close();
      } catch (IOException e) {
        // noop
      }
    }
  }

  /**
   * @param file
   *          the {@link File} for which a lock file should be created.
   * @throws IOException
   *           if a corresponding lock file already exists or could not be
   *           created.
   */
  @SuppressWarnings("resource")
  public static void createLockFile(File file) throws IOException {
    
    // we will not close this file, since it should left open
    // until JVM termination.
    RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
    FileChannel channel = randomFile.getChannel();

    // Try to get an exclusive lock on the file.
    // This method will return a lock or null, but will not block.
    // See also FileChannel.lock() for a blocking variant.
    FileLock lock = channel.tryLock();

    if (lock != null) {
      // We obtained the lock, so arrange to delete the file when
      // we're done, and then write the approximate time at which
      // we'll relinquish the lock into the file.
      file.deleteOnExit(); // Just a temporary file

      // First, we need a buffer to hold the timestamp
      ByteBuffer bytes = ByteBuffer.allocate(8); // a long is 8 bytes

      // Put the time in the buffer and flip to prepare for writing
      // Note that many Buffer methods can be "chained" like this.
      bytes.putLong(System.currentTimeMillis() + 10000).flip();

      channel.write(bytes); // Write the buffer contents to the channel
      channel.force(false);
    } else {
      throw new IOException(String.format("Lock file already exists, process probably running %s", file.getAbsolutePath()));
    }
  }

  /**
   * Transfers data from the given input stream to the provided output stream.
   * 
   * @param from an {@link InputStream} to transfer from.
   * @param to an {@link OutputStream} to transfer to.
   * @param bufsz the buffer size to use.
   * @throws IOException if an I/O error occurs.
   */
  public static void transfer(InputStream from, OutputStream to, int bufsz) throws IOException {
    byte[] buf = new byte[bufsz];
    int read;
    while ((read = from.read(buf, 0, buf.length)) > -1) {
      to.write(buf, 0, read);
    }
  }
}
