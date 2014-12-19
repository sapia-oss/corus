package org.sapia.corus.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log.Logger;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.helpers.HomePageHelper;
import org.sapia.corus.http.helpers.NotFoundHelper;
import org.sapia.ubik.rmi.server.transport.http.Handler;
import org.sapia.ubik.util.Streams;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * An instance of this class manages {@link HttpExtension}s.
 * 
 * @author yduchesne
 * 
 */
public class HttpExtensionManager implements Handler {

  public static final String STYLE_HEADER = "<style media=\"screen\" type=\"text/css\">" + "table { border-color: lightsteelblue; } "
      + "tr:hover { background-color: lightyellow; } " + "tr:first-child:hover { background-color: white; } " + "</style>";

  public static final String FOOTER = "<hr><i>Corus HTTP Service - <a href=\"http://www.sapia-oss.org/projects/corus\">www.sapia-oss.org</a></i>";

  private Logger logger;
  private ServerContext context;
  private Map<HttpExtensionInfo, HttpExtension> extensions = Collections.synchronizedMap(new HashMap<HttpExtensionInfo, HttpExtension>());

  public HttpExtensionManager(Logger logger, ServerContext context) {
    this.logger = logger;
    this.context = context;
  }

  @Override
  public void shutdown() {
  }

  /**
   * This method internally registers the given extension under the context path
   * that is provided. Then, the extension can be reached by typing a URL of the
   * following form in a browser:
   * 
   * <pre>
   * &lt;http&gt;://&lt;corus_host&gt;:&lt;corus_port&gt;/corus/ext/&lt;contextPath&gt;
   * </pre>
   * 
   * @param ext
   *          a {@link HttpExtension}.
   */
  public void addHttpExtension(HttpExtension ext) {
    HttpExtensionInfo info = ext.getInfo();
    String contextPath = info.getContextPath();
    String name = info.getName();
    if (contextPath == null) {
      throw new IllegalStateException("Context path not specified on extension info for: " + ext);
    }
    if (name == null) {
      throw new IllegalStateException("Name not specified on extension info for: " + ext);
    }
    if (!contextPath.startsWith("/")) {
      contextPath = "/" + contextPath;
    }
    info.setContextPath(contextPath);
    if (extensions.containsKey(info)) {
      throw new IllegalStateException("Extension already bound under context path: " + contextPath);
    }
    logger.debug("Adding HTTP extension under " + contextPath + ": " + ext);
    extensions.put(info, ext);
  }

  @Override
  public void handle(Request req, Response res) {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Handling request: " + req.getPath());
      }
      doHandle(req, res);
    } catch (Exception e) {
      res.setCode(HttpResponseFacade.STATUS_SERVER_ERROR);
      logger.error("Could not process HTTP request", e);
      try {
        res.close();
      } catch (IOException ioe) {
        // noop
      }
    }

  }

  private void doHandle(Request req, Response res) throws Exception {

    if (req.getPath().getSegments().length == 0) {
      synchronized (extensions) {
        HomePageHelper helper = new HomePageHelper(context, extensions.keySet());
        helper.print(req, res);
      }
    } else {
      synchronized (extensions) {
        Iterator<HttpExtensionInfo> infos = extensions.keySet().iterator();
        Path path = req.getPath();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Trying to find HTTP extension for %s", path.getPath()));
        }
        while (infos.hasNext()) {
          HttpExtensionInfo info = infos.next();
          if (path.getPath().startsWith(info.getContextPath())) {
            HttpExtension ext = (HttpExtension) extensions.get(info);
            HttpContext ctx = new HttpContext();
            ctx.setRequest(new DefaultHttpRequestFacade(req));
            ctx.setResponse(new DefaultHttpResponseFacade(res));
            ctx.setContextPath(info.getContextPath());
            if (path.getPath().equals(info.getContextPath())) {
              ctx.setPathInfo("");
            } else {
              ctx.setPathInfo(req.getPath().getPath().substring(info.getContextPath().length()));
            }
            if (logger.isDebugEnabled()) {
              logger.debug("Found extension for URI: " + path + "; path info = " + ctx.getPathInfo());
            }
            try {
              ext.process(ctx);
              return;
            } catch (FileNotFoundException e) {
              logger.error("URI not recognized: " + path);
              NotFoundHelper out = new NotFoundHelper();
              out.print(new DefaultHttpRequestFacade(req), new DefaultHttpResponseFacade(res));
              return;
            } catch (Exception e) {
              logger.error("Error caught while handling request", e);
              res.setCode(HttpResponseFacade.STATUS_SERVER_ERROR);
              Streams.closeSilently(res.getOutputStream());
              return;
            }
          }
        }
      }
    }
    logger.error("Could not find extension for URI " + req.getPath());
    NotFoundHelper out = new NotFoundHelper();
    out.print(new DefaultHttpRequestFacade(req), new DefaultHttpResponseFacade(res));
  }
}
