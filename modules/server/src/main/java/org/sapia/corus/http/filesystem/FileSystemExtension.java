package org.sapia.corus.http.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;
import org.sapia.corus.http.helpers.NotFoundHelper;

/**
 * This extension serves files from the corus.home dir. 
 * This extension can be accessed with an URL similar as the following one:
 * <p>
 * <pre>http://localhost:33000/files</pre>
 * 
 *  
 * @author yduchesne
 *
 */
public class FileSystemExtension implements HttpExtension{

  public static final String HTTP_FILESYSTEM_CONTEXT = "files";

  static final int BUFSZ = 1024; 
  
  static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
  
  private ServerContext context;
  
  public FileSystemExtension(ServerContext context) {
    this.context = context;
  }
  
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(HTTP_FILESYSTEM_CONTEXT);
    info.setName("File System");
    info.setDescription("Allows <a href=\"" + HTTP_FILESYSTEM_CONTEXT + "/\">browsing the file system</a> under CORUS_HOME");
    return info;
  }
  
  public void process(HttpContext ctx) throws Exception {
    File requested;
    if(ctx.getPathInfo().length() == 0 || ctx.getPathInfo().trim().equals("/")){
      requested = new File(context.getHomeDir());
    }
    else{
      requested = new File(context.getHomeDir()+File.separator+ctx.getPathInfo());
    }
    if(!requested.exists()){
      NotFoundHelper helper = new NotFoundHelper();
      helper.print(ctx.getRequest(), ctx.getResponse());
    }
    else{
      output(requested, ctx);      
    }
  }
  
  private void output(File requested, HttpContext ctx) throws Exception{
    if(requested.isDirectory()){
      listDir(requested, ctx);
    }
    else{
      streamFile(requested, ctx);
    }
  }
  
  private void listDir(File dir, HttpContext ctx) throws Exception{
    ctx.getResponse().setCode(200);
    ctx.getResponse().set("Content-Type", "text/html");    
    PrintStream ps = ctx.getResponse().getPrintStream();
    String title = ctx.getPathInfo();
    if(title.length() == 0){
      title = "/";
    }
    ps.println("<html><title>"+ title +"</title><body>");
    File[] fileElems = dir.listFiles();
    ps.println("<h2>" + title + "</h2>");
    if(fileElems != null && fileElems.length > 0){
      List<File> files = new ArrayList<File>();
      List<File> dirs  = new ArrayList<File>();    
      for (int i = 0; i < fileElems.length; i++) {
        if(fileElems[i].isDirectory()){
          dirs.add(fileElems[i]);
        }
        else{
          files.add(fileElems[i]);
        }
      }
      FileComparator fc = new FileComparator();
      Collections.sort(dirs, fc);    
      Collections.sort(files, fc);
      for(int i = 0; i < dirs.size(); i++){
        printFileInfo((File)dirs.get(i), ps, ctx);
      }
      for(int i = 0; i < files.size(); i++){
        printFileInfo((File)files.get(i), ps, ctx);
      }
    }
    ps.println("<p>" + HttpExtensionManager.FOOTER  +"</body></html></body></html>");
    ps.flush();
    ps.close();
  }
  
  private void streamFile(File file, HttpContext ctx) throws Exception{
    ctx.getResponse().setContentLength((int)file.length());
    if(!file.exists()){
      ctx.getResponse().setCode(404);
    }
    else{
      ctx.getResponse().set("Content-Type", MIME_TYPES.getContentType(file));
      ctx.getResponse().setCode(200);      
      FileInputStream fis = new FileInputStream(file);
      OutputStream os = null;
      try{
        byte[] buf = new byte[BUFSZ];
        os = ctx.getResponse().getOutputStream();
        int read;
        while((read = fis.read(buf, 0, buf.length)) > -1){
          os.write(buf, 0, read);
          os.flush();
        }
      }finally{
        try{
          fis.close();
        }catch(IOException e){}
        if(os != null){
          os.close();  
        }
      }

    }
  }
  
  private void printFileInfo(File file, PrintStream ps, HttpContext ctx) throws Exception{
    if(file.isDirectory()){
      ps.print("&lt;dir&gt; ");
    }
    String fName = file.isDirectory() ? file.getName() + "/" : file.getName();
    if(ctx.getPathInfo().length() == 0 || ctx.getPathInfo().equals("/")){
      ps.println("<a href=\"" + fName + "\">" + file.getName() + "</a><br>");
    }
    else{
      ps.println("<a href=\"" + fName + "\">" + file.getName() + "</a><br>");
    }
  }
  
 
  static final class FileComparator implements Comparator<File>{
    
    public int compare(File arg0, File arg1) {
      return arg0.getName().compareTo(arg1.getName());
    }
  }
  
  
}
