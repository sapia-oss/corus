package org.sapia.corus.client.rest;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.ClientDebug;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.PathTemplate;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;
import org.sapia.corus.client.common.rest.PathTemplateTree;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.CorusSecurityException.Type;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;

/**
 * An instance of this class holds instances of Java classes that have methods annotated with the {@link Path} annotation.
 * Such methods are mapped to REST resources, which an instance if this class invokes, according to the requests being
 * made. 
 * 
 * @author yduchesne
 *
 */
public class RestContainer {
  
  private static ClientDebug LOG = ClientDebug.get(RestContainer.class);
  
  private static final Set<Class<?>> JSON_RETURN_TYPES = Collects.arrayToSet(
      void.class,
      String.class,
      ProgressResult.class,
      JsonStreamable.class
  );

  /**
   * A builder of {@link RestContainer} instances.
   * 
   * @author yduchesne
   *
   */
  public static class Builder {
    
    private List<Object> resources = new ArrayList<Object>();
    
    private Builder() {
    }
    
    /**
     * @param resource a resource {@link Object}.
     * @return this instance.
     */
    public Builder resource(Object resource) {
      resources.add(resource);
      return this;
    }
    
    /**
     * @return a new instance of this class.
     */
    public static Builder newInstance() {
      return new Builder();
    }
    
    /**
     * Builds a new {@link RestContainer}, with the following resources:
     * <ul>
     *  <li> {@link ClusterResource}
     *  <li> {@link RoleResource}
     *  <li> {@link ApplicationKeyResource}
     *  <li> {@link DiagnosticResource}
     *  <li> {@link DistributionResource}
     *  <li> {@link DistributionWriteResource}
     *  <li> {@link ExecConfigResource}
     *  <li> {@link ExecConfigWriteResource}
     *  <li> {@link FileResource}
     *  <li> {@link FileWriteResource}
     *  <li> {@link PartitionResource}
     *  <li> {@link PortResource}
     *  <li> {@link PortWriteResource}
     *  <li> {@link ProcessResource}
     *  <li> {@link ProcessWriteResource}
     *  <li> {@link ProgressResource}
     *  <li> {@link PropertiesResource}
     *  <li> {@link PropertiesWriteResource}
     *  <li> {@link TagResource}
     *  <li> {@link TagWriteResource}
     *  <li> {@link MetadataResource}
     * </ul>
     * 
     * @return a new {@link RestContainer} instance.
     */
    public RestContainer buildDefaultInstance() {
      resource(new ApplicationKeyResource())
      .resource(new ClusterResource())
      .resource(new DiagnosticResource())
      .resource(new DistributionResource())
      .resource(new DistributionWriteResource())
      .resource(new ExecConfigResource())
      .resource(new ExecConfigWriteResource())
      .resource(new FileResource())
      .resource(new FileWriteResource())
      .resource(new PartitionResource())
      .resource(new PortResource())
      .resource(new PortWriteResource())
      .resource(new ProcessResource())
      .resource(new ProcessWriteResource())
      .resource(new ProgressResource())
      .resource(new PropertiesResource())
      .resource(new PropertiesWriteResource())
      .resource(new RoleResource())
      .resource(new ScriptResource())
      .resource(new TagResource())
      .resource(new TagWriteResource())
      .resource(new MetadataResource());
      return build();
    }
    
    /**
     * @return a new {@link RestContainer} instances.
     */
    public RestContainer build() {
      PathTemplateTree<RestResourceMetadata> tree = new PathTemplateTree<>();
      for (Object r : resources) {
        for (Method m : r.getClass().getDeclaredMethods()) {
          if (m.isAnnotationPresent(Path.class)) {
            Path         path     = m.getAnnotation(Path.class);
            List<PathTemplate> templates = new ArrayList<>(path.value().length);
            for (String v : path.value()) {
              PathTemplate template = PathTemplate.parse(v);
              templates.add(template);
            }
            
            Assertions.isTrue(Modifier.isPublic(m.getModifiers()), "REST resource method must be be public: %s", m);
            
            boolean valid = false;
            for (Class<?> type : JSON_RETURN_TYPES) {
              if (type.isAssignableFrom(m.getReturnType())) {
                valid = true;
                break;
              }
            }
            
            Assertions.isTrue(valid, "REST resource method must return either void, String, ProgressResult or JsonStreamable: %s", m);
            Assertions.isTrue(m.getParameterTypes().length == 1 || m.getParameterTypes().length == 2, 
                "REST resource method %s must have either first parameter of type %s, and optionally second parameter of type %s if provided", 
                m, RequestContext.class.getName(), RestResponseFacade.class);
            Assertions.isTrue(RequestContext.class.isAssignableFrom(m.getParameterTypes()[0]), 
                "REST resource method %s must have first parameter of type %s",
                m, RequestContext.class.getName());
            
            if (m.getParameterTypes().length == 2) {
              Assertions.isTrue(RestResponseFacade.class.isAssignableFrom(m.getParameterTypes()[1]), 
                  "Second parameter of REST resource method %s must have type %s if provided", 
                  m, RequestContext.class.getName());
            }
            
            String[] acceptedContentTypes;
            if (m.isAnnotationPresent(Accepts.class)) {
              acceptedContentTypes = m.getAnnotation(Accepts.class).value();
            } else {
              acceptedContentTypes = new String[]{};
            }
            
            String outputContentType = null;
            Assertions.isTrue(m.isAnnotationPresent(Output.class), "@Output annotation must be specfied on method: %s", m);
            Output output     = m.getAnnotation(Output.class);
            outputContentType = output.value();
            
            String httpMethodName = HttpMethod.GET;
            if (m.isAnnotationPresent(HttpMethod.class)) {
              httpMethodName = m.getAnnotation(HttpMethod.class).value();
            } 
            ContentLevel defaultContentLevel = ContentLevel.DETAIL;
            if (m.isAnnotationPresent(DefaultContentLevel.class)) {
              defaultContentLevel= m.getAnnotation(DefaultContentLevel.class).value();
            }
            
            for (PathTemplate t : templates) {
              RestResourceMetadata meta = new RestResourceMetadata(
                  r, m, t, defaultContentLevel,
                  acceptedContentTypes, httpMethodName, outputContentType);
              tree.addTemplate(t, meta);
              if (LOG.enabled()) {
                LOG.trace(String.format("Resource %s => %s", t, m));
              }
            }
          }
        }
      }
      
      RestContainer container = new RestContainer(tree);
      return container;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  public static class RestResourceMetadata {
    
    private Object          target;
    private Method          method;
    private PathTemplate    template;
    private ContentLevel    defaultContentLevel;
    private Set<String>     acceptedContentTypes;
    private String          httpMethodName;
    private String          outputContentType;
    private Set<Permission> permissions; 
    
    public Object getTarget() {
      return target;
    }
    
    public Method getMethod() {
      return method;
    }
    
    public ContentLevel getDefaultContentLevel() {
      return defaultContentLevel;
    }
    
    public PathTemplate getTemplate() {
      return template;
    }
    
    public Set<String> getAcceptedContentTypes() {
      return acceptedContentTypes;
    }
    
    public String getHttpMethodName() {
      return httpMethodName;
    }
    
    public String getOutputContentType() {
      return outputContentType;
    }
    
    public Set<Permission> getPermissions() {
      return permissions;
    }
    
    private RestResourceMetadata(
        Object target, 
        Method method, 
        PathTemplate template,
        ContentLevel defaultContentLevel,
        String[] acceptedContentTypes,
        String httpMethodName,
        String outputContentType) {
      this.target               = target;
      this.method               = method;
      this.template             = template;
      this.defaultContentLevel  = defaultContentLevel;
      this.acceptedContentTypes = Collects.arrayToSet(acceptedContentTypes);
      this.httpMethodName       = httpMethodName;
      this.outputContentType    = outputContentType;
      if (method.isAnnotationPresent(Authorized.class)) {
        permissions = Collects.arrayToSet(method.getAnnotation(Authorized.class).value());
        // precaution
        if (permissions.isEmpty()) {
          permissions = Collects.arrayToSet(Permission.READ);
        }
      } else {
        permissions = Collects.arrayToSet(Permission.READ);
      }
    }
    
    private boolean matches(RequestContext context) {
      return context.getRequest().getMethod().equals(httpMethodName) 
          && (context.getRequest().getAccepts().contains(outputContentType) || 
              context.getRequest().getAccepts().contains(ContentTypes.ANY))
          && (acceptedContentTypes.isEmpty() || 
              context.getRequest().getContentType() == null || 
              acceptedContentTypes.contains(context.getRequest().getContentType()));
    }
  }
  
  // --------------------------------------------------------------------------

  public static class ResourceInvocationResult  {
    
    private RestResourceMetadata  resourceMetadata;
    private OptionalValue<Object> returnValue = OptionalValue.none();
    
    public ResourceInvocationResult(RestResourceMetadata metadata, Object returnValue) {
      this.resourceMetadata = metadata;
      this.returnValue      = OptionalValue.of(returnValue);
    }
    
    public RestResourceMetadata getResourceMetadata() {
      return resourceMetadata;
    }
    
    public OptionalValue<Object> getReturnValue() {
      return returnValue;
    }
  }
  
  // --------------------------------------------------------------------------
  
  private PathTemplateTree<RestResourceMetadata> resources;
  
  private volatile boolean authRequired;
  
  /**
   * @param resources a {@link PathTemplateTree} of {@link RestResourceMetadata} instances.
   */
  private RestContainer(PathTemplateTree<RestResourceMetadata> resources) {
    this.resources  = resources;
  }
  
  /**
   * @param authRequired if <code>true</code>, forces authentication for all REST calls, even <code>GET</code> ones.
   */
  public void setAuthRequired(boolean authRequired) {
    this.authRequired = authRequired;
  }
  
  /**
   * @param context the {@link RequestContext} holding request information.
   * @param response the {@link RestResponseFacade} corresponding to the response that will be sent
   * back to the client.
   * @return the response payload to return.
   * @throws Throwable if an error occurs handling the request.
   */
  public ResourceInvocationResult invoke(final RequestContext context, RestResponseFacade response)  throws FileNotFoundException, Throwable {
    PairTuple<MatchResult, OptionalValue<RestResourceMetadata>> matchResource = resources.matches(context.getRequest().getPath(), new Condition<RestContainer.RestResourceMetadata>() {
      @Override
      public boolean apply(RestResourceMetadata meta) {
        return meta.matches(context);
      }
    });
    
    if (matchResource.getLeft().matched()) {
      RestResourceMetadata r = matchResource.getRight().get();
      Map<String, String> values = matchResource.getLeft().getValues();
      try {
        context.addParams(values);
        if (context.getSubject().isAnonymous() && authRequired) {
          throw new CorusSecurityException("Authentication is required", Type.OPERATION_NOT_AUTHORIZED);
        } 
        if (!context.getSubject().hasPermissions(r.permissions)) {
          throw new CorusSecurityException("Subject does not have required permission(s)", Type.OPERATION_NOT_AUTHORIZED);
        }
        Object payload;
        
        if (LOG.enabled()) {
          LOG.trace(String.format("Performing REST invocation on %s (method: %s) - resource: %s", r.target, r.method, r.template));
        }
        if (r.method.getParameterTypes().length == 1) {
          Assertions.illegalState(!RequestContext.class.isAssignableFrom(r.method.getParameterTypes()[0]), 
              "Method %s should have %s argument type", r.method, RequestContext.class.getName());
          payload = r.method.invoke(r.target, new Object[] { context });
        } else if (r.method.getParameterTypes().length == 2) {
          Assertions.illegalState(!RequestContext.class.isAssignableFrom(r.method.getParameterTypes()[0]), 
              "Method %s should have %s for type of first argument", r.method, RequestContext.class.getName());
          Assertions.illegalState(!RestResponseFacade.class.isAssignableFrom(r.method.getParameterTypes()[1]), 
              "Method %s should have %s for type of second argument", r.method, RestResponseFacade.class.getName());
          payload = r.method.invoke(r.target, new Object[] { context, response });
        } else {
          throw new IllegalArgumentException(String.format("Wrong method signature for %s. Expected either %s and optionally %s)" 
              + " as parameter types, in that order if both provided", r.method));
        }
        response.setContentType(r.outputContentType);
        return new ResourceInvocationResult(r, payload);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    } 
    throw new FileNotFoundException("Resource path not handled: " + context.getRequest().getPath());
  }
}



