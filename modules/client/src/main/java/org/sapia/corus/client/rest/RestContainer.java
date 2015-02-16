package org.sapia.corus.client.rest;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.rest.PathTemplate;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.CorusSecurityException.Type;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;

/**
 * An instance of this class holds instances of Java classes that have methods annotated with the {@link Path} annotation.
 * Such methods are mapped to REST resources, which an instance if this class invokes, according to the requests being
 * made. 
 * 
 * @author yduchesne
 *
 */
public class RestContainer {

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
     *  <li> {@link DistributionResource}
     *  <li> {@link DistributionWriteResource}
     *  <li> {@link ExecConfigResource}
     *  <li> {@link ExecConfigWriteResource}
     *  <li> {@link PortResource}
     *  <li> {@link PortWriteResource}
     *  <li> {@link ProcessResource}
     *  <li> {@link ProcessWriteResource}
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
      .resource(new DistributionResource())
      .resource(new DistributionWriteResource())
      .resource(new ExecConfigResource())
      .resource(new ExecConfigWriteResource())
      .resource(new PortResource())
      .resource(new PortWriteResource())
      .resource(new ProcessResource())
      .resource(new ProcessWriteResource())
      .resource(new PropertiesResource())
      .resource(new PropertiesWriteResource())
      .resource(new RoleResource())
      .resource(new TagResource())
      .resource(new TagWriteResource())
      .resource(new MetadataResource());
      return build();
    }
    
    /**
     * @return a new {@link RestContainer} instances.
     */
    public RestContainer build() {
      List<RestResourceMetadata> resourceMeta = new ArrayList<RestResourceMetadata>();
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
            Assertions.isTrue(m.getReturnType().equals(void.class) || m.getReturnType().equals(String.class), "REST resource method must return String or void: %s", m);
            Assertions.isTrue(m.getParameterTypes().length == 1, "REST resource method must have single parameter of type %s: %s", 
                RequestContext.class.getName(), m);
            Assertions.isTrue(m.getParameterTypes()[0].equals(RequestContext.class), 
                "REST resource method must have single parameter of type %s: %s", 
                RequestContext.class.getName(), m);
            
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
            for (PathTemplate t : templates) {
              resourceMeta.add(new RestResourceMetadata(
                  r, m, t, 
                  acceptedContentTypes, httpMethodName, outputContentType));
            }
          }
        }
      }
      
      Collections.sort(resourceMeta, new Comparator<RestResourceMetadata>() {
        @Override
        public int compare(RestResourceMetadata r1, RestResourceMetadata r2) {
          return r1.template.compareTo(r2.template);
        }
      });
      
      RestContainer container = new RestContainer(resourceMeta);
      return container;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  private static class RestResourceMetadata {
    
    private Object          target;
    private Method          method;
    private PathTemplate    template;
    private Set<String>     acceptedContentTypes;
    private String          httpMethodName;
    private String          outputContentType;
    private Set<Permission> permissions; 
    
    private RestResourceMetadata(
        Object target, 
        Method method, 
        PathTemplate template,
        String[] acceptedContentTypes,
        String httpMethodName,
        String outputContentType) {
      this.target               = target;
      this.method               = method;
      this.template             = template;
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
    
    private Map<String, String> matches(RequestContext context, String[] pathValues) {
      MatchResult result = template.matches(pathValues);
      boolean matched = result.matched() 
          && context.getRequest().getMethod().equals(httpMethodName) 
          && (context.getRequest().getAccepts().contains(outputContentType) || 
              context.getRequest().getAccepts().contains(ContentTypes.ANY))
          && (acceptedContentTypes.isEmpty() || 
              context.getRequest().getContentType() == null || 
              acceptedContentTypes.contains(context.getRequest().getContentType()));
      if (matched) {
        return result.getValues();
      } else {
        return null;
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  private List<RestResourceMetadata> resources  = new ArrayList<RestResourceMetadata>();
  
  private volatile boolean authRequired;
  
  /**
   * @param resources a {@link List} of {@link RestResourceMetadata} instances.
   */
  private RestContainer(List<RestResourceMetadata> resources) {
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
  public String invoke(RequestContext context, RestResponseFacade response)  throws FileNotFoundException, Throwable {
    String[] pathValues = StringUtils.split(context.getRequest().getPath(), "/");
    for (RestResourceMetadata r : resources) {
      Map<String, String> values = r.matches(context, pathValues);
      if (values != null) {
        try {
          context.addParams(values);
          if (context.getSubject().isAnonymous() && authRequired) {
            throw new CorusSecurityException("Authentication is required", Type.OPERATION_NOT_AUTHORIZED);
          } 
          if (!context.getSubject().hasPermissions(r.permissions)) {
            throw new CorusSecurityException("Subject does not have required permission(s)", Type.OPERATION_NOT_AUTHORIZED);
          }
          String payload = (String) r.method.invoke(r.target, new Object[] { context });
          response.setContentType(r.outputContentType);
          return payload;
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      }
    }
    throw new FileNotFoundException("Resource path not handled: " + context.getRequest().getPath());
  }
  
}
