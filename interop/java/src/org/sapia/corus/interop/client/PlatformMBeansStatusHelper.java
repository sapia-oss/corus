package org.sapia.corus.interop.client;

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

import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;

public class PlatformMBeansStatusHelper{
  
  public static void process(Context ctx){
    
    /////// RUNTIME

    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    
    param("runtime.startTime",  
        new Long(runtime.getStartTime()), ctx);
    param("runtime.startDate",  
        new Date(runtime.getStartTime()), ctx);    
    param("runtime.upTime",  
        new Long(runtime.getUptime()), ctx);
    
    /////// MEMORY

    MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
    
    param("mem.pendingFinalization.count", 
        new Integer(mem.getObjectPendingFinalizationCount()), ctx);
    
    // heap
    
    param("mem.heap.init", 
        new Long(mem.getHeapMemoryUsage().getInit()), ctx);
    param("mem.heap.committed", 
        new Long(mem.getHeapMemoryUsage().getCommitted()), ctx);    
    param("mem.heap.max", 
        new Long(mem.getHeapMemoryUsage().getMax()), ctx);    
    param("mem.heap.used", 
        new Long(mem.getHeapMemoryUsage().getUsed()), ctx);    
    
    // non-heap
    
    param("mem.non-heap.init", 
        new Long(mem.getNonHeapMemoryUsage().getInit()), ctx);
    param("mem.non-heap.committed", 
        new Long(mem.getNonHeapMemoryUsage().getCommitted()), ctx);    
    param("mem.non-heap.max", 
        new Long(mem.getNonHeapMemoryUsage().getMax()), ctx);
    param("mem.non-heap.used", 
        new Long(mem.getNonHeapMemoryUsage().getUsed()), ctx);    
    
    /////// GC
    
    List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

    try{
      param("gc.count", getLongTotal(gcs, "getCollectionCount"), ctx);
      param("gc.elapsedTime", getLongTotal(gcs, "getCollectionTime"), ctx);
    }catch(Exception e){}
    
    /////// THREADS

    ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    param("threads.count",  
        new Integer(threads.getThreadCount()), ctx);    
    param("threads.daemon.count",  
        new Integer(threads.getDaemonThreadCount()), ctx);
    param("threads.peak.count",  
        new Integer(threads.getPeakThreadCount()), ctx);    
    param("threads.totalStarted.count",  
        new Long(threads.getTotalStartedThreadCount()), ctx);
    
    /////// CLASSLOADING

    ClassLoadingMXBean classes = ManagementFactory.getClassLoadingMXBean();

    param("classes.loadedCount",  
        new Integer(classes.getLoadedClassCount()), ctx);    
    param("class.totalLoadedCount",  
        new Long(classes.getTotalLoadedClassCount()), ctx);
    param("classes.unloadedCount",  
        new Long(classes.getUnloadedClassCount()), ctx);    
    
    /////// OS

    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    param("os.name", os.getName(), ctx);    
    param("os.version", os.getVersion(), ctx);    
    param("os.arch", os.getArch(), ctx);    
    param("os.processors",  
        new Integer(os.getAvailableProcessors()), ctx);    

  }
  
  private static void param(String name, Object value, Context ctx){
    Param p = new Param();
    p.setName(name);
    p.setValue(value.toString());
    ctx.addParam(p);
  }
  
  private static Object getLongTotal(List<GarbageCollectorMXBean> gcs, String getter) throws Exception{
    long total = 0;
    for(int i = 0; i < gcs.size(); i++){
      GarbageCollectorMXBean gc = (GarbageCollectorMXBean)gcs.get(i);
      Method meth = gc.getClass().getMethod(getter, new Class[0]);
      meth.setAccessible(true);
      total = total + 
        ((Long)(meth.invoke(gc, new Object[0]))).longValue();
    }
    return new Long(total);
  }  

}
