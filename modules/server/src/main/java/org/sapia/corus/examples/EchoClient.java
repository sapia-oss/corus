/*
 * EchoClient.java
 *
 * Created on October 18, 2005, 2:34 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author yduchesne
 */
public class EchoClient {
  
  /** Creates a new instance of EchoClient */
  public EchoClient() {
  }
  
  public static void main(String[] args){
    try{
      Socket socket;
      socket = new Socket(args[0], Integer.parseInt(args[1]));
      //socket = new Socket("localhost", 7777);
      handleClient(socket);
    }catch(Exception e){
      System.out.println("Terminated");
      e.printStackTrace();
    }
  }
  
  private static void handleClient(Socket socket) throws Exception{
    String line;
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));    
    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
    while((line = reader.readLine()) != null){
      if(line.equals("quit")){
        socket.close();
        break;
      }
      pw.println(line);
      pw.flush();
      System.out.println("RESPONSE: " + response.readLine());
    }
  }
  
}
