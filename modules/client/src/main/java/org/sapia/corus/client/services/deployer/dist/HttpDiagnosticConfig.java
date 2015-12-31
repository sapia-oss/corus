package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeAtLeast;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeGreater;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Implements HTTP-based diagnostic configuration.
 * 
 * @author yduchesne
 *
 */
public class HttpDiagnosticConfig extends DiagnosticConfig implements ObjectCreationCallback {
  
  public static final String ELEMENT_NAME = "http-diagnostic";
  
  public static final String PROTOCOL_HTTP = "http";
  
  public static final String ROOT_PATH = "/";
 
  public static final int DEFAULT_SUCCESS_CODE              = 200;
  public static final int DEFAULT_CONNECTION_Timeout_MILLIS = 3000;
  public static final int DEFAULT_READ_Timeout_MILLIS       = 3000;
  
  private String path              = ROOT_PATH;
  private int    successCode       = DEFAULT_SUCCESS_CODE;
  private int    connectionTimeout = DEFAULT_CONNECTION_Timeout_MILLIS;
  private int    readTimeout       = DEFAULT_READ_Timeout_MILLIS;
  private int    portPrefix;
  
  public HttpDiagnosticConfig() {
    super(PROTOCOL_HTTP);
  }

  protected HttpDiagnosticConfig(String protocol) {
    super(PROTOCOL_HTTP);
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  
  public String getPath() {
    if (path == null) {
      path = ROOT_PATH;
    }
    return path;
  }

  public void setSuccessCode(int code) {
    this.successCode = code;
  }

  public int getSuccessCode() {
    return successCode;
  }
  
  public void setPortPrefix(int prefix) {
    portPrefix = prefix;
  }
  
  public int getPortPrefix() {
    return portPrefix;
  }
  
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }
  
  public int getConnectionTimeout() {
    return connectionTimeout;
  }
  
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }
  
  public int getReadTimeout() {
    return readTimeout;
  }
  
  // --------------------------------------------------------------------------
  // ObjectCreationCallback
  
  @Override
  public Object onCreate() throws ConfigurationException {
    doValidate(ELEMENT_NAME);
    return this;
  }
  
  protected void doValidate(String elementName) throws ConfigurationException {
    attributeGreater(elementName, "successCode", 0, successCode);
    attributeAtLeast(elementName, "portPrefix", 0, portPrefix);
    attributeGreater(elementName, "connectionTimeout", 0, connectionTimeout);
    attributeGreater(elementName, "readTimeout", 0, readTimeout);
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(path);
    out.writeInt(successCode);
    out.writeInt(portPrefix);
    out.writeInt(connectionTimeout);
    out.writeInt(readTimeout);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
    path              = in.readUTF();
    successCode       = in.readInt();
    portPrefix        = in.readInt();
    connectionTimeout = in.readInt();
    readTimeout       = in.readInt();
  }

}
