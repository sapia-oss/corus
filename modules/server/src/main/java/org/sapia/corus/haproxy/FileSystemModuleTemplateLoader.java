package org.sapia.corus.haproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.sapia.corus.client.services.file.FileSystemModule;

import freemarker.cache.TemplateLoader;

/**
 * Implements {@link TemplateLoader} over {@link FileSystemModule}.
 * 
 * @author yduchesne
 *
 */
public class FileSystemModuleTemplateLoader implements TemplateLoader  { 
   
  private File             templateBaseDir;
  private FileSystemModule fs;
  
  @Override
  public long getLastModified(Object src) {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public void closeTemplateSource(Object src) throws IOException {
    
  }
  
  @Override
  public Object findTemplateSource(String name) throws IOException { 
    File templateFile = new File(templateBaseDir, name);
    if (fs.exists(templateFile)) {
      return templateFile;
    }

    throw new FileNotFoundException("Could not find file: " + name);
  }
  
  @Override
  public Reader getReader(Object src, String encoding) throws IOException {
    File file = (File) src;
    return new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
  }
  

}
