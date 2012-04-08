package org.sapia.corus.http.helpers;

import java.io.IOException;
import java.io.PrintStream;

import org.sapia.corus.http.HttpExtensionManager;

import simple.http.Request;
import simple.http.Response;

/**
 * Generates HTML content for the 404 error page.
 * 
 * @author yduchesne
 *
 */
public class NotFoundHelper implements OutputHelper{
  
  public void print(Request req, Response res) throws Exception{
    try{
      res.set("Content-Type", "text/html");
      res.setCode(404);
      PrintStream ps = res.getPrintStream();
      ps.println("<html><body><h1>404 - NOT FOUND</h1> " + HttpExtensionManager.FOOTER + "</body></html>");
      ps.flush();
      ps.close();
      res.commit();
    }catch(IOException e){
      try{
        res.getOutputStream().close();
        res.commit();
      }catch(IOException e2){}
    }    
  }
}
