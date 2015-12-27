package org.sapia.corus.client.rest.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles deployment and undeployment of files.
 * 
 * @author yduchesne
 *
 */
public class FileWriteResource extends ResourceSupport {
  
  private static final int BUFSZ = 8192;
  
  // --------------------------------------------------------------------------
  //  deploy

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult deployFileForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doDeployFileForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/files/{corus:name}",
    "/clusters/{corus:cluster}/hosts/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult deployFileForCluster(RequestContext context) throws Exception {
    return doDeployFileForCluster(context, ClusterInfo.clustered());
  }
  
  private ProgressResult doDeployFileForCluster(RequestContext context, ClusterInfo cluster) throws Exception {
    String fileName = context.getRequest().getValue("corus:name").asString();
    Value checksum = context.getRequest().getValue("checksum-md5");
    DeployPreferences prefs = DeployPreferences.newInstance();
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
    File file = transfer(context, fileName);
    try {
      return doDeployFile(context, file, prefs, cluster);
    } finally {
      file.delete();
    }
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult deployFileForHost(RequestContext context) throws Exception {
    String fileName = context.getRequest().getValue("corus:name").asString();
    File file = transfer(context, fileName);
    try {
      ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
      Value checksum = context.getRequest().getValue("checksum-md5");
      DeployPreferences prefs = DeployPreferences.newInstance();
      if (checksum.isSet()) {
        prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
      }
      return doDeployFile(context, file, prefs, cluster);
    } finally {
      file.delete();
    }
  }
  
  // --------------------------------------------------------------------------
  //  undeploy

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult undeployFileForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doUndeployFile(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/files/{corus:name}",
    "/clusters/{corus:cluster}/hosts/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult undeployFileForCluster(RequestContext context) throws Exception {
    return doUndeployFile(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult undeployFileForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doUndeployFile(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // restricted
  
  private ProgressResult doDeployFile(RequestContext context, File toDeploy, DeployPreferences prefs, ClusterInfo cluster) throws Exception {
    Value destDir = context.getRequest().getValue("d");
    return progress(context.getConnector().getDeployerFacade().deployFile(toDeploy.getAbsolutePath(), destDir.isSet() ? destDir.asString() : null, prefs, cluster));
  }

  private ProgressResult doUndeployFile(RequestContext context, ClusterInfo cluster) throws Exception {
    FileCriteria criteria = FileCriteria.newInstance();
    criteria.setName(ArgMatchers.parse(context.getRequest().getValue("corus:name").notNull().asString()));  
    return progress(context.getConnector().getFileManagementFacade().deleteFiles(criteria, cluster));
  }
  
  protected File transfer(RequestContext ctx, String fileName) throws IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    if (!tmpDir.exists()) {
      throw new IllegalStateException("Directory corresponding to java.io.tmpdir does not exist");
    }
    File                tmpFile = new File(tmpDir, fileName);
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
