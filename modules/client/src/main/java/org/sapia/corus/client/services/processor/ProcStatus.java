package org.sapia.corus.client.services.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;

/**
 * @author Yanick Duchesne
 */
public class ProcStatus extends Status implements JsonStreamable {

  static final long serialVersionUID = 1L;

  private String corusPid;
  private List<Context> contexts;

  public ProcStatus(Process proc) {
    corusPid = proc.getProcessID();
    if (proc.getProcessStatus() == null) {
      contexts = new ArrayList<Context>(0);
    } else {
      contexts = proc.getProcessStatus().getContexts();
    }
  }

  public List<Context> getContexts() {
    return contexts;
  }

  public String getProcessID() {
    return corusPid;
  }
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject().field("processId").value(corusPid);
    stream.beginArray();
    for (Context c : contexts) {
      stream.beginObject().field("").value(c.getName());
      stream.beginArray();
      for (Param p : c.getParams()) {
        stream.beginObject()
          .field("name").value(p.getName())
          .field("value").value(p.getValue())
        .endObject();
      }
      stream.endArray();
      stream.endObject();
    }
    stream.endArray();
    stream.endObject();
  }

}
