/*
 * EchoServer.java
 *
 * Created on October 18, 2005, 2:12 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author yduchesne
 */
public class EchoServer extends Thread{
  
  private ServerSocket _server;
  private List _clients = Collections.synchronizedList(new ArrayList());
  
  /** Creates a new instance of EchoServer */
  public EchoServer(ServerSocket server) {
    _server = server;
  }
  
  public void close() throws IOException{
    _server.close();
  }
  
  public void run(){
    while(true){
      try{
        System.out.println("EchoServer::accept...");
        Socket client = _server.accept();
        System.out.println("EchoServer::client connection");        
        ClientThread ct = new ClientThread(client, _clients);
        _clients.add(ct);
        ct.start();
      }catch(Exception e){
        e.printStackTrace();
        break;
      }
    }
    System.out.println("EchoServer::exiting...");            
  }
  
  public static void main(String[] args){
    try{
      int port = Integer.parseInt(args[0]);
      EchoServer server = new EchoServer(new ServerSocket(port));
      server.start();
      Runtime.getRuntime().addShutdownHook(new Shutdown(server));
    }catch(Exception e){
      e.printStackTrace();
    }
  
  }
  
  /////////// INNER CLASSES ////////////
  
  public static class ClientThread extends Thread{
    
    Socket _client;
    private List _queue;

    public ClientThread(Socket client, List queue){
      _client = client;
      _queue = queue;
    }
    
    public void run(){

      BufferedReader reader;
      PrintWriter pw;
      try{
        reader = new BufferedReader(new InputStreamReader(_client.getInputStream()));      
        pw = new PrintWriter(_client.getOutputStream(), true);        
      }catch(IOException e){
        System.out.println("Client::terminated");              
        _queue.remove(this);
        return;
      }
      while(true){
        try{
          String line;
          while((line = reader.readLine()) != null){
            System.out.println("Client::ECHO: " + line);
            if(System.getProperty(line) != null){
             pw.println("ECHO: " + System.getProperty(line));
            }
            else{
              pw.println("ECHO: " + line);
            }
            pw.flush();
          }
        }catch(IOException e){
          e.printStackTrace();
          try{
            _client.close();
          }catch(IOException e2){}
          break;
        }
      }
      System.out.println("Client::terminated");              
      _queue.remove(this);
    }
  }
  
  public static class Shutdown extends Thread{
    
    private EchoServer _server;
    
    public Shutdown(EchoServer server){
      _server = server;
    }
    
    public void run(){
      try{
        _server.close();
      }catch(Exception e){}
    }
  }
}
