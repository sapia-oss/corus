package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.console.table.Cell;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.docker.DockerClientException;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.client.services.docker.DockerContainer;
import org.sapia.ubik.util.Collects;

/**
 * Allows basic interaction with the Docker daemon, through Corus.
 * 
 * @author yduchesne
 *
 */
public class Docker extends CorusCliCommand {
  
  private static final int BUZSZ = 8092;
  
  private static final int IMAGE_ID_ABBREVIATION_LEN     = 20;
  private static final int IMAGE_ID_ABBR_PADDING         = 7;
  private static final int CONTAINER_ID_ABBREVIATION_LEN = 10;
  private static final int CONTAINER_ID_ABBR_PADDING     = 3;
  
  public static final OptionDef OPT_PATH       = new OptionDef("f", true);
  public static final OptionDef OPT_IMAGE_NAME = new OptionDef("n", true);

  private static final String ARG_LS     = "ls";
  private static final String ARG_SAVE   = "save";
  private static final String ARG_PS     = "ps";
  private static final String ARG_RM     = "rm";
  private static final String ARG_PULL   = "pull";
  
  
  private static final String[] AVAILABLE_COMMANDS = new String[] {
      ARG_PULL, ARG_LS, ARG_SAVE, ARG_PS, ARG_RM
  };
  
  private static final List<OptionDef> AVAILABLE_OPTIONS = Collections.unmodifiableList(Collects.arrayToList(
     OPT_IMAGE_NAME, OPT_PATH, OPT_CLUSTER
  ));
  
  private static final TableDef IMAGE_TBL = TableDef.newInstance()
      .createCol("id", 20).createCol("creation", 15).createCol("tags", 40);

  private static final TableDef CONTAINER_TBL = TableDef.newInstance()
      .createCol("id", 10).createCol("creation", 10).createCol("image", 15).createCol("names", 40);
  
  private static TableDef TITLE_TBL  = TableDef.newInstance()
      .createCol("val", 78);
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAILABLE_OPTIONS;
  }

  @Override
  protected void doInit(CliContext context) {
    IMAGE_TBL.setTableWidth(context.getConsole().getWidth());
  }

  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    String command = ctx.getCommandLine().assertNextArg(AVAILABLE_COMMANDS).getName();
    if (command.equals(ARG_RM)) {
      doRemoveImages(ctx, ctx.getCommandLine().getOptNotNull(OPT_IMAGE_NAME.getName()).getValueNotNull());
    } else if (command.equals(ARG_LS)) {
      doListImages(ctx, ctx.getCommandLine().getOptOrDefault(OPT_IMAGE_NAME.getName(), "**").getValueNotNull());
    } else if (command.equals(ARG_PS)) {
      doListContainers(ctx, ctx.getCommandLine().getOptOrDefault(OPT_IMAGE_NAME.getName(), "**").getValueNotNull());
    } else if (command.equals(ARG_SAVE)) {
      doSaveImage(
          ctx,
          ctx.getCommandLine().getOptNotNull(OPT_IMAGE_NAME.getName()).getValueNotNull(),
          ctx.getCommandLine().getOptNotNull(OPT_PATH.getName()).getValueNotNull()
      );
    } else if (command.equals(ARG_PULL)) {
      doPullImage(ctx, ctx.getCommandLine().getOptNotNull(OPT_IMAGE_NAME.getName()).getValueNotNull());
    } else {
      throw new InputException("Unknown argument: " + command + ". Expected one of: " 
          + ToStringUtil.joinToString((Object[]) AVAILABLE_COMMANDS));
    }
  }

  // --------------------------------------------------------------------------
  // Write methods

  private void doRemoveImages(CliContext ctx, String imageName) {
    displayProgress(
        ctx.getCorus().getDockerManagementFacade().removeImages(ArgMatchers.parse(imageName), getClusterInfo(ctx)), 
        ctx
    );
  }
  
  private void doSaveImage(CliContext ctx, String imageName, String path) throws AbortException, InputException {
    File file = ctx.getFileSystem().getFile(path);
    FilePath filePath = FilePath.forFile(file.getAbsolutePath());
    FilePath dirPath  = filePath.getDirectoriesAsPath();
    if (dirPath.notEmpty()) {
      File dirs = dirPath.createFile();
      if (!dirs.exists()) {
        dirs.mkdirs();
      }
      if (!dirs.exists()) {
        throw new InputException("Could not create directory: " + dirs.getAbsolutePath());
      }
    }
    ctx.getConsole().println("Fetching image " + imageName);
    try (InputStream is = ctx.getCorus().getDockerManagementFacade().getImagePayload(imageName)) {
      try (FileOutputStream os = new FileOutputStream(filePath.createFile())) {
        IOUtil.transfer(is, os, BUZSZ);
        os.flush();
      }
      ctx.getConsole().println(String.format("Saved image %s to %s", imageName, file.getAbsolutePath()));
    } catch (DockerClientException | IOException e) {
      ctx.getConsole().println(ctx.createAndAddErrorFor(this, "Could not save Docker image", e).getSimpleMessage());
    }
  }
  
  private void doPullImage(CliContext ctx, String imageName) throws AbortException, InputException {
    displayProgress(ctx.getCorus().getDockerManagementFacade().pullImage(imageName, getClusterInfo(ctx)), ctx);
  }
  
  // --------------------------------------------------------------------------
  // List containers
  
  private void doListContainers(CliContext ctx, String imageName) {
    try {
      Results<List<DockerContainer>> results = ctx.getCorus().getDockerManagementFacade().getContainers(ArgMatchers.parse(imageName), getClusterInfo(ctx));
      displayContainerResults(results, ctx);
    } catch (DockerClientException e) {
      ctx.getConsole().println(ctx.createAndAddErrorFor(this, "Error occurred trying to list Docker coptainer(s)", e).getSimpleMessage());
    }
  }
 
  private void displayContainerResults(Results<List<DockerContainer>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<DockerContainer>> result = res.next();
      displayContainerHeader(result.getOrigin(), ctx);
      for (DockerContainer cnt : result.getData()) {
        displayContainer(cnt, ctx);
      }
    }
  }
  
  private void displayContainerHeader(CorusHost addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = CONTAINER_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();
    headers.getCellAt(CONTAINER_TBL.col("id").index()).append("ID");
    headers.getCellAt(CONTAINER_TBL.col("creation").index()).append("Creation");
    headers.getCellAt(CONTAINER_TBL.col("image").index()).append("Image");
    headers.getCellAt(CONTAINER_TBL.col("names").index()).append("Names");
    headers.flush();
  }
  
  private void displayContainer(DockerContainer cnt, CliContext ctx) {
    Table imgTable = CONTAINER_TBL.createTable(ctx.getConsole().out());
    
    imgTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = imgTable.newRow();
    row.getCellAt(CONTAINER_TBL.col("id").index()).append(ToStringUtil.abbreviate(
        cnt.getId(), 
        CONTAINER_ID_ABBREVIATION_LEN, CONTAINER_ID_ABBR_PADDING, CONTAINER_ID_ABBR_PADDING
    ));
    row.getCellAt(CONTAINER_TBL.col("creation").index()).append(cnt.getCreationTimeStamp());
    row.getCellAt(CONTAINER_TBL.col("image").index()).append(cnt.getImageName());
    
    List<String> names = cnt.getNames();
    for (String n : names) {
      Cell cell = row.getCellAt(CONTAINER_TBL.col("names").index());
      cell.append(n);
      row.flush();
      row = imgTable.newRow();
    }
    
    row.flush();
  }
  
  // --------------------------------------------------------------------------
  // List images
  
  private void doListImages(CliContext ctx, String imageName) {
    try {
      Results<List<DockerImage>> results = ctx.getCorus().getDockerManagementFacade().getImages(ArgMatchers.parse(imageName), getClusterInfo(ctx));
      displayImageResults(results, ctx);
    } catch (DockerClientException e) {
      ctx.getConsole().println(ctx.createAndAddErrorFor(this, "Error occurred trying to list Docker image(s)", e).getSimpleMessage());
    }
  }
  
  private void displayImageResults(Results<List<DockerImage>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<DockerImage>> result = res.next();
      displayImageHeader(result.getOrigin(), ctx);
      for (DockerImage img : result.getData()) {
        displayImage(img, ctx);
      }
    }
  }

  private void displayImageHeader(CorusHost addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = IMAGE_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();
    headers.getCellAt(IMAGE_TBL.col("id").index()).append("ID");
    headers.getCellAt(IMAGE_TBL.col("creation").index()).append("Creation");
    headers.getCellAt(IMAGE_TBL.col("tags").index()).append("Tags");
    headers.flush();
  }
  
  private void displayImage(DockerImage img, CliContext ctx) {
    Table imgTable = IMAGE_TBL.createTable(ctx.getConsole().out());
    
    imgTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = imgTable.newRow();
    row.getCellAt(IMAGE_TBL.col("id").index()).append(ToStringUtil.abbreviate(
        img.getId(), 
        IMAGE_ID_ABBREVIATION_LEN, IMAGE_ID_ABBR_PADDING, IMAGE_ID_ABBR_PADDING 
    ));
    row.getCellAt(IMAGE_TBL.col("creation").index()).append(img.getCreationTimeStamp());
    Set<String> tags = img.getTags();

    for (String t : tags) {
      Cell cell = row.getCellAt(IMAGE_TBL.col("tags").index());
      cell.append(t);
      row.flush();
      row = imgTable.newRow();
    }
    
    row.flush();
  }
}
