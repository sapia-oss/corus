package org.sapia.corus.interop.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class MockStreamConnection implements StreamConnection {
  ByteArrayOutputStream _response;
  MockStreamListener    _listener;

  public MockStreamConnection(MockStreamListener listener) {
    _listener = listener;
  }

  public void close() throws IOException {
  }

  public InputStream getInputStream() throws IOException {
    if (_response == null) {
      throw new IOException("getOutputStream() must be called prior to getInputStream()");
    }

    ByteArrayInputStream resp = new ByteArrayInputStream(_response.toByteArray());
    _response = null;

    return resp;
  }

  public OutputStream getOutputStream() throws IOException {
    return new MockOutputStream(this);
  }

  static final class MockOutputStream extends ByteArrayOutputStream {
    MockStreamConnection _owner;

    MockOutputStream(MockStreamConnection owner) {
      _owner = owner;
    }

    public void close() throws IOException {
      super.close();
      _owner._response = new ByteArrayOutputStream();
      _owner._listener.onRequest(new ByteArrayInputStream(toByteArray()),
                                 _owner._response);
    }
  }
}
