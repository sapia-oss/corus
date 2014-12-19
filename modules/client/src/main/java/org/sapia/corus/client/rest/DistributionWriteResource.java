package org.sapia.corus.client.rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles deploy, undeploy.
 * 
 * @author yduchesne
 *
 */
public class DistributionWriteResource {
  
  private static final int BUFSZ = 8192;
 
  // --------------------------------------------------------------------------
  //  deploy
  
  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void deployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    File        file    = transfer(context);
    try {
      context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), cluster);
    } finally {
      file.delete();
    }
  }

  @Path({
    "/clusters/hosts/{corus:host}/distributions", 
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void deployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    File        file    = transfer(context);
    try {
      context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), cluster);
    } finally {
      file.delete();
    }
  }  
  
  // --------------------------------------------------------------------------
  // undeploy

  @Path({
    "/clusters/distributions", 
    "/clusters/{corus:cluster}/distributions",
    "/clusters/hosts/distributions", 
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgFactory.parse(context.getRequest().getValue("d").asString()))
        .version(ArgFactory.parse(context.getRequest().getValue("v").asString()))
        .build();
    context.getConnector().getDeployerFacade().undeployDistribution(criteria, cluster);
  }  
  
  @Path({
    "/clusters/hosts/{corus:host}/distributions", 
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgFactory.parse(context.getRequest().getValue("d").asString()))
        .version(ArgFactory.parse(context.getRequest().getValue("v").asString()))
        .build();
    context.getConnector().getDeployerFacade().undeployDistribution(criteria, cluster);
  }  
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  protected File transfer(RequestContext ctx) throws IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    if (!tmpDir.exists()) {
      throw new IllegalStateException("Directory corresponding to java.io.tmpdir does not exist");
    }
    File                tmpFile = new File(tmpDir, UUID.randomUUID() + ".zip");
    FileOutputStream    fos     = null;
    BufferedInputStream bis     = null;
    try {
      byte[] buf  = new byte[BUFSZ];
      int    read = 0;
      fos = new FileOutputStream(tmpFile);
      bis = new BufferedInputStream(ctx.getRequest().getContent(), BUFSZ);
      while ((read = bis.read(buf)) > -1) {
        fos.write(buf, 0, read);
      }
      return tmpFile;
    } catch (IOException e) {
      tmpFile.delete();
      throw e;
    } finally {
      if (fos != null) {
        try {
          fos.flush();
          fos.close();
        } catch (Exception e2) {
          // NOOP
        }
      }
      if (bis != null) {
        try {
          bis.close();
        } catch (Exception e2) {
          // NOOP
        }
      }
    }
  }
  
}
