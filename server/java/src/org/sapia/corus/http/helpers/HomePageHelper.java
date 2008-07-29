package org.sapia.corus.http.helpers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.http.HttpExtensionInfo;
import org.sapia.corus.http.HttpExtensionManager;

import simple.http.Request;
import simple.http.Response;

public class HomePageHelper {
  
  List _extensionInfos = new ArrayList();
  
  public HomePageHelper(Collection extInfos){
    _extensionInfos.addAll(extInfos);
  }
  
  public void print(Request req, Response res){
    try{
      res.set("Content-Type", "text/html");
      res.setCode(200);
      PrintStream ps = res.getPrintStream();
      ps.println("<html><body><h1>Welcome to Corus @ " + 
          CorusRuntime.getCorus().getDomain() + "</h1> ");
      ps.println("<h2>Available Extensions</h2>");
      ps.println("<table border=\"0\">");
      ps.println("<th>Context Path</th><th>Name</th><th>Description</th>");
      Collections.sort(_extensionInfos);
      for(int i = 0; i < _extensionInfos.size(); i++){
        ps.print("<tr>");
        HttpExtensionInfo info = (HttpExtensionInfo)_extensionInfos.get(i);
        
        ps.print("<td>");
        ps.print(info.getContextPath());
        ps.print("</td>");

        ps.print("<td><b>");
        ps.print(info.getName());
        ps.print("</b></td>");        
        
        ps.print("<td>");
        if(info.getDescription() != null){
          ps.print(info.getDescription());
          ps.print(".");
        }
        ps.print("</td>");        
        
        ps.println("</tr>");        
      }
      ps.println("</table>");
      ps.println(HttpExtensionManager.FOOTER);
      ps.println("</body></html>");
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
