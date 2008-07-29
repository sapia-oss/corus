package org.sapia.corus.deployer.transport.mplex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.deployer.DeploymentMetadata;
import org.sapia.corus.deployer.transport.AbstractDeploymentClient;
import org.sapia.corus.deployer.transport.DeploymentClient;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

/**
 * Implements the <code>DeploymentClient</code> interface over the Mplex transport.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MplexDeploymentClient extends AbstractDeploymentClient implements DeploymentClient{
	
	static final int BUFSZ = 2048;
	
	private Socket _sock;
	
	/**
	 * @param addr a <code>ServerAddress</code>.
	 * @throws IOException if no connection could be made.
	 */
	public void connect(ServerAddress addr) throws IOException{
		TCPAddress tcpAddr = (TCPAddress)addr;
		_sock = new Socket(tcpAddr.getHost(), tcpAddr.getPort());
	}
	
	/**
	 * @see DeploymentClient#deploy(String, long, Set, InputStream)
	 *
	public ProgressQueue deploy(String fileName, 
	                            long contentLen, 
	                           Set targets,
	                           InputStream is) throws IOException{
	                           	
  	                           	
                           	
		if(_sock == null){
			throw new IOException("This instance is not connected; call connect()");
		}
		
    DeploymentMetadata meta = new DeploymentMetadata(fileName, contentLen, targets, false);
    OutputStream os = _sock.getOutputStream();
		os.write(MplexDeploymentAcceptor.DEPLOY_STREAM_HEADER.getBytes("UTF-8"));
		os.flush();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(meta);
    oos.flush();
    byte[] buf = new byte[BUFSZ];
    long total = 0; 
    int read = 0;
   
    while((read = is.read(buf, 0, BUFSZ)) > 0 && total < contentLen){
    	total = total + read;
    	os.write(buf, 0, read);
    	os.flush();
    }
  
    ObjectInputStream ois = new ObjectInputStream(_sock.getInputStream());
    try{
      ProgressQueue queue = (ProgressQueue)ois.readObject();
      return queue;
    }catch(ClassNotFoundException e){
    	throw new IOException("Could not deserialize response: " + e.getMessage());
    }
	}*/
	
	/**
	 * @see DeploymentClient#close()
	 */
	public void close(){
		if(_sock != null){
			try {
        _sock.close();
      } catch (Exception e) {
        // noop
      }
		}
	}
	
  /**
   * @see org.sapia.corus.deployer.transport.AbstractDeploymentClient#getInputStream()
   */
  protected InputStream getInputStream() throws IOException {
		if(_sock == null){
			throw new IOException("This instance is not connected; call connect()");
		}
		return _sock.getInputStream();
  }

  /**
   * @see org.sapia.corus.deployer.transport.AbstractDeploymentClient#getOutputStream()
   */
  protected OutputStream getOutputStream() throws IOException {
		if(_sock == null){
			throw new IOException("This instance is not connected; call connect()");
		}
		OutputStream os = _sock.getOutputStream();
		os.write(MplexDeploymentAcceptor.DEPLOY_STREAM_HEADER.getBytes("UTF-8"));
		os.flush();
		return os;
  }
  
	public static void main(String[] args) {
		try {
			TCPAddress addr = new TCPAddress("127.0.0.1", 33000);
			File toDeploy = new File("dist/demoDist.jar");
			FileOutputStream fos = null;
			MplexDeploymentClient client = null;      
			try{
				client = new MplexDeploymentClient();
				client.connect(addr);
				Set targets = new HashSet();
				targets.add(new TCPAddress("127.0.0.1", 33100));
				DeploymentMetadata meta = new DeploymentMetadata(toDeploy.getName(), toDeploy.length(), targets, true);
				ProgressQueue queue = client.deploy(meta, new FileInputStream(toDeploy));
				List msgs;
				while(queue.hasNext()){
					msgs = queue.next();
					for(int i = 0; i < msgs.size(); i++){
						System.out.println(((ProgressMsg)msgs.get(i)).getMessage());
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(client != null){
					client.close();
				}
				if(fos != null){
					try {
						fos.close();
					} catch (Exception e) {
						// noop
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
