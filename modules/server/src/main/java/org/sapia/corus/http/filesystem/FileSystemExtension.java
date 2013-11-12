package org.sapia.corus.http.filesystem;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;
import org.sapia.corus.http.helpers.NotFoundHelper;
import org.sapia.ubik.rmi.interceptor.Interceptor;

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
public class FileSystemExtension implements HttpExtension, Interceptor {

  public static final String HTTP_FILESYSTEM_CONTEXT = "files";
  
  private static final String PARAMETER_ACTION = "action";
  private static final String ACTION_LIST      = "list";
  private static final String ACTION_COMPRESS  = "compress";
  
  static final int BUFSZ = 1024; 
  
  static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  private ServerContext context;
  
  private Map<String, String> symlinks;
  
  public FileSystemExtension(ServerContext context) {
    this.context = context;
    
    symlinks = new ConcurrentHashMap<String, String>();
    for (Map.Entry<Object, Object> entry: context.getCorusProperties().entrySet()) {
    	String key = (String) entry.getKey();
    	if (key.startsWith(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX)) {
    		symlinks.put(key.substring(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX.length()), (String) entry.getValue()); 
    	}
    }
    
    Properties serverProps = context.getServices().getConfigurator().getProperties(PropertyScope.SERVER);
    for (String propName : serverProps.stringPropertyNames()) {
      if (propName.startsWith(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX)) {
        symlinks.put(propName.substring(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX.length()), serverProps.getProperty(propName)); 
      }
    }
    
    context.getServices().getEventDispatcher().addInterceptor(PropertyChangeEvent.class, this);
  }
  
  /**
   * @param event a {@link PropertyChangeEvent}.
   */
  public void onPropertyChangeEvent(PropertyChangeEvent event) {
    if (event.getScope() == PropertyScope.SERVER) {
      if (event.getName().startsWith(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX)) {
        String linkName = event.getName().substring(CorusConsts.PROPERTY_CORUS_FILE_LINK_PREFIX.length());
        if (event.getType() == Type.ADD) {
          if (log.isDebugEnabled()) {
            log.debug("Adding new symbolic link " + linkName);
          }
          symlinks.put(linkName, event.getValue()); 
        } else {
          if (log.isDebugEnabled()) {
            log.debug("Removing symbolic link " + linkName);
          }
          symlinks.remove(linkName); 
        }
      }
    }
  }
  
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(HTTP_FILESYSTEM_CONTEXT);
    info.setName("File System");
    info.setDescription("Allows <a href=\"" + HTTP_FILESYSTEM_CONTEXT + "/\">browsing the file system</a> under $CORUS_HOME");
    return info;
  }
  
  public void process(HttpContext ctx) throws Exception {
    File requested;
    if (ctx.getPathInfo().length() == 0 || ctx.getPathInfo().trim().equals("/")) {
      requested = new File(context.getHomeDir());
    }
    else{
      // Extract first path element for symlinks
      String linkDestination = null;
      int rootPathIndex = ctx.getPathInfo().indexOf('/', 1);
      if (rootPathIndex > 1) {
        linkDestination = symlinks.get(ctx.getPathInfo().substring(1, rootPathIndex));
      }
      
      if (linkDestination != null) {
        requested = new File(linkDestination + ctx.getPathInfo().substring(rootPathIndex));
      } else {
        requested = new File(context.getHomeDir()+File.separator+ctx.getPathInfo());
      }
    }
    
    if (!requested.exists()) {
      NotFoundHelper helper = new NotFoundHelper();
      helper.print(ctx.getRequest(), ctx.getResponse());
    }
    else{
      output(requested, ctx);      
    }
  }
  
  private void output(File requested, HttpContext ctx) throws Exception {
    String action = ctx.getRequest().getParameter(PARAMETER_ACTION);

    if (ACTION_COMPRESS.equals(action)) {
      compressDirContent(requested, ctx);
      
    } else {
      // ACTION_LIST: Default action
      if (requested.isDirectory()) {
        listDir(requested, ctx);
      } else {
        streamFile(requested, true, ctx);
      }
    }
  }
  
  private void listDir(File dir, HttpContext ctx) throws Exception{
    ctx.getResponse().setStatusCode(200);
    ctx.getResponse().setHeader("Content-Type", "text/html");    
    PrintStream ps = new PrintStream(ctx.getResponse().getOutputStream());
    String title = ctx.getPathInfo();
    if(title.length() == 0){
      title = "/";
    }
    ps.println("<html><title>Corus Files - "+ title +"</title><body>");

    List<String> pathElements = new ArrayList<String>();
    for (StringTokenizer tokenizer = new StringTokenizer(title, "/", false); tokenizer.hasMoreTokens(); ) {
      pathElements.add(tokenizer.nextToken());
    }

    String hrefValue = ctx.getContextPath() + "/";
    ps.print("<h3>Directory <a href=\"" + hrefValue + "\">$CORUS_HOME</a>/");
    for (int i = 0; i < pathElements.size(); i++) {
      hrefValue += pathElements.get(i) + "/";
      
      if (i < pathElements.size()-1) {
        ps.print("<a href=\"" + hrefValue + "\">" + pathElements.get(i) + "</a>/");
      } else {
        ps.println(pathElements.get(i));
      }
    }
    ps.println("</h3>");
    
    ps.println("<b>Actions:</b>");
    ps.println("<ul><li><a href=\"?action=" + ACTION_COMPRESS + "\">Zip files and sub directories</a></li></ul>");
    
    File[] fileElems = dir.listFiles();
    if(fileElems != null && fileElems.length > 0){
      List<FileEntry> files = new ArrayList<FileEntry>();
      List<FileEntry> dirs  = new ArrayList<FileEntry>();    
      for (int i = 0; i < fileElems.length; i++) {
        if(fileElems[i].isDirectory()){
          dirs.add(FileEntry.createNew(fileElems[i]));
        }
        else{
          files.add(FileEntry.createNew(fileElems[i]));
        }
      }
      
      // Adding symbolic links
      if (dir.getAbsolutePath().equals(context.getHomeDir())) {
    	  for (Map.Entry<String, String> linkEntry: symlinks.entrySet()) {
    		  dirs.add(FileEntry.createNewLink(linkEntry.getKey(), new File(linkEntry.getValue())));
    	  }
      }
      
      FileComparator fc = new FileComparator();
      Collections.sort(dirs, fc);    
      Collections.sort(files, fc);
      
      ps.println("<p><b>Content:</b></p><ul>");
      ps.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" width=\"80%\">");
      ps.println("<th width=\"5%\"></th><th width=\"40%\">Name</th><th width=\"10%\">Size</th><th width=\"15%\">Last Modified</th>");
      for(int i = 0; i < dirs.size(); i++){
        printFileInfo(dirs.get(i), ps, ctx);
      }
      for(int i = 0; i < files.size(); i++){
        printFileInfo(files.get(i), ps, ctx);
      }
      ps.println("</table></ul><br/>");
    }
    ps.println("<p>" + HttpExtensionManager.FOOTER  +"</body></html></body></html>");
    ps.flush();
    ps.close();
  }
  
  private void streamFile(File file, boolean isTextContent, HttpContext ctx) throws Exception{
    ctx.getResponse().setContentLength((int) file.length());
    if(!file.exists()){
      ctx.getResponse().setStatusCode(HttpResponseFacade.STATUS_NOT_FOUND);
    }
    else {
      if (isTextContent) {
        ctx.getResponse().setHeader("Content-Type", "text/plain;charset=UTF-8");
      } else {
        ctx.getResponse().setHeader("Content-Type", MIME_TYPES.getContentType(file));
        ctx.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      }
      ctx.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);      
      FileInputStream fis = new FileInputStream(file);

      log.info("Sending out file " + file.getName() + " (" + formatFileSize(file) + ") ...");
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
        log.info("Streaming of file " + file.getName() + " is completed");
      }

    }
  }
  
  private void printFileInfo(FileEntry file, PrintStream ps, HttpContext ctx) throws Exception {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    ps.print("<tr><td align=\"center\">");
    if (file.isLink()) {
      ps.print("ln");
    } else if (file.isDirectory()) {
      ps.print("dir");
    }
    ps.print("</td>");
    
    ps.print("<td>");
    String fName = file.isDirectory() ? file.getName() + "/" : file.getName();
    if (file.isLink() && !file.getFile().exists()) {
      ps.println(file.getName());
    } else {
      ps.println("<a href=\"" + fName + "?action=" + ACTION_LIST + "\">" + file.getName() + "</a>");
    }
    ps.print("</td>");
    
    ps.print("<td align=\"right\">");
    if (!file.isLink() && !file.isDirectory()) {
      ps.print(formatFileSize(file.getFile()));
    }
    ps.print("</td>");
    
    ps.print("<td align=\"center\">");
    ps.print(dateFormatter.format(new Date(file.getFile().lastModified())));
    ps.print("</td>");
  }
  
  private void compressDirContent(File file, HttpContext ctx) throws Exception {
    String domainName = System.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN);
    String tmpDir = context.getServices().getDeployer().getConfiguration().getTempDir();
    String hostName = context.getCorusHost().getHostName();
    String dirName = file.getName();
    
    int counter = 0;
    File zipFile = new File(tmpDir, dirName + "@" + domainName + "-" + hostName + ".zip");
    while (zipFile.exists()) {
      String suffix = String.valueOf(++counter);
      while (suffix.length() < 4) {
        suffix = "0" + suffix;
      }
      
      zipFile = new File(tmpDir, dirName + "@" + domainName + "-" + hostName + "_" + suffix +".zip");
    }
    
    FileSystemModule fsService = context.getServices().getFileSystem();
    try {
      log.debug("Compressing content of directory " + file.getName() + " into file: " + zipFile);
      fsService.zip(file, zipFile);
      streamFile(zipFile, false, ctx);
    } finally {
      fsService.deleteFile(zipFile);
      log.debug("Deleted zip file: " + zipFile);
    }
  }
  
  private String formatFileSize(File aFile) {
    long fileLengthBytes = aFile.length();
    
    if (fileLengthBytes < 1048576L) {
      double lengthKilo = ((long) (fileLengthBytes / 102.4d)) / 10d; 
      return lengthKilo + " KB";
    } else if (fileLengthBytes < 1073741824L) {
      double lengthMega = ((long) (fileLengthBytes / 104857.6d)) / 10d; 
      return lengthMega + " MB";
    } else {
      double lengthGiga = ((long) (fileLengthBytes / 107374182.4d)) / 10d; 
      return lengthGiga + " GB";
    }
    
  }
  
  static final class FileComparator implements Comparator<FileEntry>{
	  public int compare(FileEntry arg0, FileEntry arg1) {
	    return arg0.getName().compareTo(arg1.getName());
	  }
  }
  
  
}
