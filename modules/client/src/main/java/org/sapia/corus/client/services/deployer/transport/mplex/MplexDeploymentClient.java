package org.sapia.corus.client.services.deployer.transport.mplex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient;
import org.sapia.corus.client.services.deployer.transport.DeploymentClient;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * Implements the {@link DeploymentClient} interface over the Mplex transport.
 * 
 * @author Yanick Duchesne
 */
public class MplexDeploymentClient extends AbstractDeploymentClient implements DeploymentClient{
  
  public static final String DEPLOY_STREAM_HEADER = "corus/deployer";
	
	static final int BUFSZ = 2048;
	
	private Socket sock;
	
	/**
	 * @param addr a {@link ServerAddress}.
	 * @throws IOException if no connection could be made.
	 */
	public void connect(ServerAddress addr) throws IOException{
		TCPAddress tcpAddr = (TCPAddress)addr;
		sock = new Socket(tcpAddr.getHost(), tcpAddr.getPort());
	}
	
	/**
	 * @see DeploymentClient#close()
	 */
	public void close(){
		if(sock != null){
			try {
        sock.close();
      } catch (Exception e) {
        // noop
      }
		}
	}
	
  /**
   * @see org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient#getInputStream()
   */
  protected InputStream getInputStream() throws IOException {
		if(sock == null){
			throw new IOException("This instance is not connected; call connect()");
		}
		return sock.getInputStream();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient#getOutputStream()
   */
  protected OutputStream getOutputStream() throws IOException {
		if(sock == null){
			throw new IOException("This instance is not connected; call connect()");
		}
		OutputStream os = sock.getOutputStream();
		os.write(DEPLOY_STREAM_HEADER.getBytes("UTF-8"));
		os.flush();
		return os;
  }
}
