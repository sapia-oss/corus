package org.sapia.corus.cloud.aws.image.userdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;

public class UserDataBuilder {
  
  private List<String> lines = new ArrayList<String>();
  private boolean executable = false;
  
  public UserDataBuilder line(String line) {
    lines.add(line + "\n");
    return this;
  }
  
  public UserDataBuilder executable() {
    executable = true;
    return this;
  }
  
  public String toByte64(WorkflowLog log) {
    StringBuilder content = new StringBuilder();
    if (executable) {
      content.append("#!/bin/sh\n");
    }
    for (String l : lines) {
      content.append(l);
    }
   
    String contentString = content.toString();
    log.verbose("User data: ");
    log.verbose("--------------------------------------------------------");
    log.verbose(contentString);
    log.verbose("--------------------------------------------------------");
    return new Base64().encodeToString(contentString.getBytes());
  }

  public static UserDataBuilder newInstance() {
    return new UserDataBuilder();
  }

  
}