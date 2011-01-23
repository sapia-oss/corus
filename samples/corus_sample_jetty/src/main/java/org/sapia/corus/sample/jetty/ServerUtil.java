package org.sapia.corus.sample.jetty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerUtil {
  
  public static File getWebappsRootDir(){
    File f = new File(
      System.getProperty("user.dir") + 
      File.separator + 
      "webapps");
    
    if(!f.exists()){
      throw new IllegalStateException(
          String.format(
              "Webapp directory not found: %s", 
              f.getAbsolutePath()
          )
       );
    }
    
    return f;    
  }
  
  public static Handler loadWebapps(){
    return loadWebapps(null);
  }
  
  public static Handler loadWebapps(SessionHandler sessionHandler){
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    File webAppsRootDir = getWebappsRootDir();
    File[] webAppsDirs = webAppsRootDir.listFiles();
    List<Handler> handlers = new ArrayList<Handler>();
    for(File webAppFileOrDir: webAppsDirs){
      WebAppContext ctx = new WebAppContext();
      if(webAppFileOrDir.getName().endsWith(".war")){
        String contextPath = "/"+webAppFileOrDir.getName().replace(".war", ""); 
        System.out.println(String.format("Adding WAR: %s; context path: %s", webAppFileOrDir.getAbsolutePath(), contextPath));
        ctx.setContextPath(contextPath);
        ctx.setWar(webAppFileOrDir.getAbsolutePath());
      }
      else{
        String contextPath = "/"+webAppFileOrDir.getName(); 
        System.out.println(String.format("Adding webapp: %s", contextPath));
        ctx.setContextPath(contextPath);
        ctx.setDescriptor(
            webAppFileOrDir.getAbsolutePath()+
            File.separator+
            "WEB-INF"+
            File.separator+
            "web.xml");
        ctx.setResourceBase(webAppFileOrDir.getAbsolutePath());
      }
      if(sessionHandler != null){
        ctx.setSessionHandler(sessionHandler);
      }
        
      ctx.setParentLoaderPriority(false);
      handlers.add(ctx);
    }
    contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));
    return contexts;    
  }

}
