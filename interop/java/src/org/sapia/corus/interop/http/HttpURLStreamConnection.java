package org.sapia.corus.interop.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import org.sapia.corus.interop.helpers.StreamConnection;


/**
 * Implements the <code>StreamConnection</code> interface over a
 * <code>java.net.URLConnection</code>.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpURLStreamConnection implements StreamConnection {

  private HttpURLConnection _conn;
  private InputStream   _is;
  private OutputStream  _os;

  /**
   * Creates an instance of this class with the given URL. Internally
   * uses the URL to create a <code>URLConnection</code>.
   *
   * @param url a <code>URL</code>
   * @throws IOException if this instance could not be created.
   * @throws IllegalArgumentException If the URL passed in is not http.
   */
  public HttpURLStreamConnection(URL url) throws IOException {
    if (!url.getProtocol().equals("http")) {
      throw new IllegalArgumentException("The URL passed in does not use the HTTP protocol: " + url);
    }
    
    _conn = (HttpURLConnection) url.openConnection();
    _conn.setDoInput(true);
    _conn.setDoOutput(true);
    _conn.setUseCaches(false);
    _conn.setRequestMethod("POST");
  }

  /**
   * @see StreamConnection#close()
   */
  public void close() {
    if (_is != null) {
      try {
        _is.close();
      } catch (IOException e) {
        //noop
      }
    }

    if (_os != null) {
      try {
        _os.close();
      } catch (IOException e) {
        //noop
      }
    }
  }

  /**
   * @see StreamConnection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return _is = _conn.getInputStream();
  }

  /**
   * @see StreamConnection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return _os = _conn.getOutputStream();
  }
}
