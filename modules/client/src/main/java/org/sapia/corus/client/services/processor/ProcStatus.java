package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.interop.api.message.ContextMessagePart;
import org.sapia.corus.interop.api.message.ParamMessagePart;
import org.sapia.corus.interop.soap.message.Context;

/**
 * @author Yanick Duchesne
 */
public class ProcStatus implements JsonStreamable, Externalizable {

  static final long serialVersionUID = 1L;

  private String corusPid;
  private List<ContextMessagePart> contexts;

  /**
   * DO NOT CALL: for externalization only
   */
  public ProcStatus() {
  }
  
  public ProcStatus(Process proc) {
    corusPid = proc.getProcessID();
    if (proc.getProcessStatus().isNull()) {
      contexts = new ArrayList<ContextMessagePart>(0);
    } else {
      // copying messages to SOAP-specific implementation, since its message classes are 
      // serializable
      contexts = new ArrayList<ContextMessagePart>(proc.getProcessStatus().get().getContexts().size());
      for (ContextMessagePart c : proc.getProcessStatus().get().getContexts()) {
        ContextMessagePart.Builder contextBuilder = new Context.ContextBuilder().name(c.getName());
        for (ParamMessagePart p : c.getParams()) {
          contextBuilder.param(p.getName(), p.getValue());
        }
        contexts.add(contextBuilder.build());
      }
    }
  }

  public List<ContextMessagePart> getContexts() {
    return contexts;
  }

  public String getProcessID() {
    return corusPid;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable

  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject().field("processId").value(corusPid);
    stream.beginArray();
    for (ContextMessagePart c : contexts) {
      stream.beginObject().field("").value(c.getName());
      stream.beginArray();
      for (ParamMessagePart p : c.getParams()) {
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
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    corusPid = in.readUTF();
    contexts = (List<ContextMessagePart>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(corusPid);
    out.writeObject(contexts);
  }


}
