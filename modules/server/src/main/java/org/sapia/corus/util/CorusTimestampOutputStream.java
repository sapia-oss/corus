package org.sapia.corus.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link PrintStream} that writes a timestamp at the start of each line.
 * 
 * @author yduchesne
 *
 */
public class CorusTimestampOutputStream extends PrintStream {
  
  private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
  
  public CorusTimestampOutputStream(OutputStream delegate) {
    super(delegate, true);
  }
  
  @Override
  public void println() {
    timestamp();
    println();
  }
  
  @Override
  public void println(boolean arg0) {
    timestamp();
    super.println(arg0);
  }
  
  @Override
  public void println(char x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(char[] x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(double x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(float x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(int x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(long x) {
    timestamp();
    super.println(x);
  }

  @Override
  public void println(Object x) {
    timestamp();
    super.println(x);
  }
  
  @Override
  public void println(String x) {
    timestamp();
    super.println(x);
  }
  
  private synchronized void timestamp() {
    super.println("[" + format.format(new Date()) + "] ");
  }

}
