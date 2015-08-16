package org.sapia.corus.http.jmx;

import java.io.PrintStream;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.TCPAddress;

/**
 * This extension emits status gathered from the Corus server VM. The Output
 * format is XML. This extension can be accessed with an URL similar as the
 * following one:
 * <p>
 * 
 * <pre>
 * http://localhost:33000/jmx
 * </pre>
 * 
 * @author yduchesne
 * 
 */
public class JmxExtension implements HttpExtension {

  public static final String HTTP_JMX_CONTEXT = "jmx";

  private ServerContext context;

  public JmxExtension(ServerContext context) {
    this.context = context;
  }

  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(HTTP_JMX_CONTEXT);
    info.setName("Corus JMX Introspection");
    info.setDescription("Returns the <a href=\"" + HTTP_JMX_CONTEXT + "\">status of this Corus server</a>, by introspecting its platform MBeans");
    return info;
  }

  @Override
  public void process(HttpContext ctx) throws Exception {
    ctx.getResponse().setHeader("Content-Type", "text/xml");
    try {
      doProcess(ctx);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
  
  @Override
  public void destroy() {
  }

  private void doProcess(HttpContext ctx) throws Exception {

    PrintStream ps = new PrintStream(ctx.getResponse().getOutputStream());
    ps.print("<vmStatus");
    xmlAttribute("domain", context.getDomain(), ps);
    try {
      TCPAddress addr = context.getCorusHost().getEndpoint().getServerTcpAddress();
      xmlAttribute("host", addr.getHost(), ps);
      xmlAttribute("port", Integer.toString(addr.getPort()), ps);
    } catch (ClassCastException e) {
    }
    long time = System.currentTimeMillis();
    xmlAttribute("creationTime", Long.toString(time), ps);
    xmlAttribute("creationDate", new Date(time), ps);
    ps.println(">");

    // ///// RUNTIME

    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

    attribute("runtime.startTime", new Long(runtime.getStartTime()), ps);
    attribute("runtime.startDate", new Date(runtime.getStartTime()), ps);
    attribute("runtime.upTime", new Long(runtime.getUptime()), ps);

    // ///// MEMORY

    MemoryMXBean mem = ManagementFactory.getMemoryMXBean();

    attribute("mem.pendingFinalization.count", new Integer(mem.getObjectPendingFinalizationCount()), ps);

    // heap

    attribute("mem.heap.init", new Long(mem.getHeapMemoryUsage().getInit()), ps);
    attribute("mem.heap.committed", new Long(mem.getHeapMemoryUsage().getCommitted()), ps);
    attribute("mem.heap.max", new Long(mem.getHeapMemoryUsage().getMax()), ps);
    attribute("mem.heap.used", new Long(mem.getHeapMemoryUsage().getUsed()), ps);

    // non-heap

    attribute("mem.non-heap.init", new Long(mem.getNonHeapMemoryUsage().getInit()), ps);
    attribute("mem.non-heap.committed", new Long(mem.getNonHeapMemoryUsage().getCommitted()), ps);
    attribute("mem.non-heap.max", new Long(mem.getNonHeapMemoryUsage().getMax()), ps);
    attribute("mem.non-heap.used", new Long(mem.getNonHeapMemoryUsage().getUsed()), ps);

    // ///// GC

    List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

    attribute("gc.count", getLongTotal(gcs, "getCollectionCount"), ps);
    attribute("gc.elapsedTime", getLongTotal(gcs, "getCollectionTime"), ps);

    // ///// THREADS

    ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    attribute("threads.count", new Integer(threads.getThreadCount()), ps);
    attribute("threads.daemon.count", new Integer(threads.getDaemonThreadCount()), ps);
    attribute("threads.peak.count", new Integer(threads.getPeakThreadCount()), ps);
    attribute("threads.totalStarted.count", new Long(threads.getTotalStartedThreadCount()), ps);

    // ///// CLASSLOADING

    ClassLoadingMXBean classes = ManagementFactory.getClassLoadingMXBean();

    attribute("classes.loadedCount", new Integer(classes.getLoadedClassCount()), ps);
    attribute("class.totalLoadedCount", new Long(classes.getTotalLoadedClassCount()), ps);
    attribute("classes.unloadedCount", new Long(classes.getUnloadedClassCount()), ps);

    // ///// OS

    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    attribute("os.name", os.getName(), ps);
    attribute("os.version", os.getVersion(), ps);
    attribute("os.arch", os.getArch(), ps);
    attribute("os.processors", new Integer(os.getAvailableProcessors()), ps);

    ps.println("</vmStatus>");
    ps.flush();
    ps.close();
    ctx.getResponse().commit();
  }

  private void attribute(String name, Object value, PrintStream ps) {
    ps.print("<attribute name=\"");
    ps.print(name);
    ps.print("\" value=\"");
    ps.print(value);
    ps.print("\" />");
  }

  private void xmlAttribute(String name, Object value, PrintStream ps) {
    ps.print(" ");
    ps.print(name);
    ps.print("=\"");
    ps.print(value);
    ps.print("\"");
  }

  private Object getLongTotal(List<GarbageCollectorMXBean> gcs, String getter) throws Exception {
    long total = 0;
    for (int i = 0; i < gcs.size(); i++) {
      GarbageCollectorMXBean gc = gcs.get(i);
      Method meth = gc.getClass().getMethod(getter, new Class[0]);
      meth.setAccessible(true);
      total = total + ((Long) (meth.invoke(gc, new Object[0]))).longValue();
    }
    return new Long(total);
  }

}
