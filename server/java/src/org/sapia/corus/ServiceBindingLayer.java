package org.sapia.corus;

import org.sapia.soto.Layer;
import org.sapia.soto.ServiceMetaData;


public class ServiceBindingLayer implements Layer{
  
  public void init(ServiceMetaData meta) throws Exception {
    if(meta.getServiceID() != null){
      InitContext.get().getServerContext().getServices().bind(
          meta.getServiceID(), 
          meta.getService());
    }
  }
  
  public void start(ServiceMetaData meta) throws Exception {}

  public void dispose() {}

}
