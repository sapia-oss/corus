package org.sapia.corus.client.rest.resources;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.DefaultContentLevel;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.ResourceNotFoundException;
import org.sapia.corus.client.rest.PartitionService.PartitionSet;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.TimeValue;

/**
 * This resources provides logic for the management of {@link PartitionSet}s.
 * 
 * @author yduchesne
 *
 */
public class PartitionResource {
  
  @Path({
    "/partitionsets"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public PartitionSet createPartitionSet(RequestContext context) {
    
    List<ArgMatcher> includes = new ArrayList<>();
    List<ArgMatcher> excludes = new ArrayList<>();
    
    Value includeList  = context.getRequest().getValue("includes");
    Value excludeList  = context.getRequest().getValue("excludes");
    Value batchSize    = context.getRequest().getValue("partitionSize");
    Value timeoutSecs  = context.getRequest().getValue(
        "timeout", 
        Long.toString(PartitionService.DEFAULT_PARTITION_SET_TIMEOUT.getValueInSeconds())
    );
    
    if (!includeList.isNull()) {
      for (String pattern : includeList.asList()) {
        includes.add(ArgMatchers.parse(pattern));
      }
    }
    
    if (!excludeList.isNull()) {
      for (String pattern : excludeList.asList()) {
        excludes.add(ArgMatchers.parse(pattern));
      }
    }
    
    PartitionSet partitions = context.getPartitionService().createPartitionSet(
        batchSize.asInt(), includes, excludes, context.getConnector(), TimeValue.createSeconds(timeoutSecs.asInt())
    );

    return partitions;

  }

  @Path({
    "/partitionsets/{corus:partitionSetId}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.READ)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public PartitionSet getPartitionSet(RequestContext context) {
    String id = context.getRequest().getValue("corus:partitionSetId").asString();
    try {
      PartitionSet partitions = context.getPartitionService().getPartitionSet(id);
      return partitions;
    } catch (IllegalArgumentException e) {
      throw new ResourceNotFoundException("Could not find resource for partition set: " + id);
    }
  }
  
  @Path({
    "/partitionsets/{corus:partitionSetId}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Authorized(Permission.WRITE)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public void deletePartitionSet(RequestContext context) {
    String id = context.getRequest().getValue("corus:partitionSetId").asString();
    context.getPartitionService().deletePartitionSet(id);
  }
}
