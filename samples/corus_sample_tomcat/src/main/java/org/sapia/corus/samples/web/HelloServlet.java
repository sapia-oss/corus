package org.sapia.corus.samples.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A basic "hello world" servlet.
 * 
 * @author yduchesne
 *
 */
public class HelloServlet extends HttpServlet {
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/html");
  
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter(sw);
    
    pw.println("<html><body>");
    pw.println("<h1>Sample Web App</h1>");
    pw.println("<p>Hello World</p>");
    pw.println("</body></html>");
    pw.flush();
    
    byte[] toWrite = sw.getBuffer().toString().getBytes();
    
    res.setContentLength(toWrite.length);
    res.getOutputStream().write(toWrite);
    res.getOutputStream().flush();
    res.getOutputStream().close();
  }

}
