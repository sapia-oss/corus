package org.sapia.corus.client.cli.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.console.AbortException;
import org.sapia.console.CmdElement;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Func;

/**
 * @author yduchesne
 * 
 */
public class Conf extends CorusCliCommand {

  private static final String PASSWORD_PATTERN = "password";
  private static final String PASSWORD_OBFUSCATION = "********";

  private final TableDef PROPS_TBL = TableDef.newInstance().createCol("name", 31).createCol("val", 31).createCol("cat", 10);

  private final TableDef TAGS_TBL = TableDef.newInstance().createCol("val", 78);

  private final TableDef TITLE_TBL = TableDef.newInstance().createCol("val", 78);

  // --------------------------------------------------------------------------

  public static final String ARG_ADD    = "add";
  public static final String ARG_RENAME = "ren";
  public static final String ARG_DEL    = "del";
  public static final String ARG_LS     = "ls";
  public static final String ARG_EXPORT = "export";
  public static final String ARG_MERGE  = "merge";
  public static final String ARG_ALL    = "all";

  private static final OptionDef OPT_PROPERTY   = new OptionDef("p", false);
  private static final OptionDef OPT_CATEGORY   = new OptionDef("c", false);
  private static final OptionDef OPT_FILE       = new OptionDef("f", true);
  private static final OptionDef OPT_BASE       = new OptionDef("b", true);
  private static final OptionDef OPT_TAG        = new OptionDef("t", false);
  private static final OptionDef OPT_REPLACE    = new OptionDef("r", false);
  private static final OptionDef OPT_SCOPE      = new OptionDef("s", false);
  private static final OptionDef OPT_SCOPE_SVR  = new OptionDef("s", false);
  private static final OptionDef OPT_SCOPE_PROC = new OptionDef("p", false);
  private static final OptionDef OPT_CLEAR      = new OptionDef("clear", false);
  
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_PROPERTY, OPT_CATEGORY, OPT_FILE, OPT_BASE, OPT_TAG, OPT_REPLACE, 
      OPT_SCOPE, OPT_SCOPE_SVR, OPT_SCOPE_PROC, OPT_CLEAR, OPT_CLUSTER
  );

  // --------------------------------------------------------------------------

  private enum Op {
    ADD, DELETE, LIST, EXPORT, RENAME, MERGE
  }

  // --------------------------------------------------------------------------
  
  @Override
  protected void doInit(CliContext context) {
    PROPS_TBL.setTableWidth(context.getConsole().getWidth());
    TAGS_TBL.setTableWidth(context.getConsole().getWidth());
    TITLE_TBL.setTableWidth(context.getConsole().getWidth());
  }  

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    Op op = null;
    if (ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()) {
      String opArg = ctx.getCommandLine().assertNextArg().getName();
      if (opArg.equalsIgnoreCase(ARG_ADD)) {
        op = Op.ADD;
      } else if (opArg.equalsIgnoreCase(ARG_DEL)) {
        op = Op.DELETE;
      } else if (opArg.equalsIgnoreCase(ARG_LS)) {
        op = Op.LIST;
      } else if (opArg.equalsIgnoreCase(ARG_EXPORT)) {
        op = Op.EXPORT;
      } else if (opArg.equalsIgnoreCase(ARG_RENAME)) {
        op = Op.RENAME;
      } else if (opArg.equalsIgnoreCase(ARG_MERGE)) {
        op = Op.MERGE;
      } else {
        throw new InputException("Unknown argument " + opArg + "; expecting one of: add | del | ls");
      }
    } else {
      throw new InputException("Missing argument; expecting one of: add | del | ls");
    }

    if (op != null) {
      if (ctx.getCommandLine().containsOption(OPT_TAG.getName(), false)) {
        try {
          handleTag(op, ctx);
        } catch (IOException e) {
          throw new AbortException("I/O Error performing command", e);
        }
      } else {
        try {
          handlePropertyOp(op, ctx);
        } catch (IOException e) {
          throw new AbortException("I/O Error performing command", e);
        }
      }
    }
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  private void handleTag(Op op, CliContext ctx) throws InputException, IOException {
    if (op == Op.ADD) {
      String tagString = ctx.getCommandLine().assertOption(OPT_TAG.getName(), true).getValue();
      Set<String> toAdd = new HashSet<String>();
      File tagFile = ctx.getFileSystem().getFile(tagString);
      if (tagFile.exists()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile)));
        String line = null;
        try {
          while ((line = reader.readLine()) != null) {
            toAdd.addAll(Collects.arrayToSet(line.split(",")));
          }
        } finally {
          reader.close();
        }
      } else {
        toAdd.addAll(Collects.arrayToSet(tagString.split(",")));
      }
      ctx.getCorus().getConfigFacade().addTags(toAdd, getClusterInfo(ctx));
    } else if (op == Op.DELETE) {
      String toRemove = ctx.getCommandLine().assertOption(OPT_TAG.getName(), true).getValue();
      ctx.getCorus().getConfigFacade().removeTag(toRemove, getClusterInfo(ctx));
    } else if (op == Op.LIST) {
      Results<Set<Tag>> tags = ctx.getCorus().getConfigFacade().getTags(getClusterInfo(ctx));
      Results<List<Tag>> sorted = Sorting.sortSet(tags, Tag.class, ctx.getSortSwitches());
      displayTagResults(sorted, ctx);
    } else if (op == Op.EXPORT) {
      exportTagResults(ctx.getCorus().getConfigFacade().getTags(getClusterInfo(ctx)), ctx);
    } else if (op == Op.RENAME) {
      String tagString = ctx.getCommandLine().assertOption(OPT_TAG.getName(), true).getValue();
      String[] tagNameValues = tagString.split(",");
      List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
      for (String nv : tagNameValues) {
        String[] nameValuePair = nv.split("=");
        if (nameValuePair.length == 2) {
          nvPairs.add(new NameValuePair(nameValuePair[0], nameValuePair[1]));
        }
      }
      ctx.getCorus().getConfigFacade().renameTags(nvPairs, getClusterInfo(ctx));
    } 
  }

  private void handlePropertyOp(Op op, CliContext ctx) throws InputException, IOException {
    PropertyScope scope = PropertyScope.PROCESS;
    if (ctx.getCommandLine().containsOption(OPT_SCOPE.getName(), true)) {
      String scopeOpt = ctx.getCommandLine().assertOption(OPT_SCOPE.getName(), true).getValue();
      if (scopeOpt.startsWith(OPT_SCOPE_PROC.getName())) {
        scope = PropertyScope.PROCESS;
      } else if (scopeOpt.startsWith(OPT_SCOPE_SVR.getName())) {
        scope = PropertyScope.SERVER;
      } else {
        throw new InputException("Scope (-s) not valid, expecting s[vr] or p[roc]");
      }
    }
    if (op == Op.ADD) {
      String pair = ctx.getCommandLine().assertOption(OPT_PROPERTY.getName(), true).getValue();
      if (!pair.contains("=")) {
        File propFile = ctx.getFileSystem().getFile(pair);
        if (!propFile.exists()) {
          throw new InputException("File does not exist: " + pair);
        }
        if (propFile.isDirectory()) {
          throw new InputException("File is a directory: " + pair);
        }

        Properties props = new Properties();
        InputStream input = null;
        try {
          input = new FileInputStream(propFile);
          props.load(input);
          boolean clearExisting = ctx.getCommandLine().containsOption(OPT_CLEAR.getName(), false);
          ctx.getCorus().getConfigFacade().addProperties(scope, props, categorySet(ctx), clearExisting, getClusterInfo(ctx));
        } catch (IOException e) {
          CliError err = ctx.createAndAddErrorFor(this, e);
          ctx.getConsole().println(err.getSimpleMessage());
        } finally {
          try {
            input.close();
          } catch (IOException e) {
          }
        }
      } else {
        int index = pair.indexOf('=');
        if (index > 0) {
          String name = pair.substring(0, index);
          String value = pair.substring(1 + index);
          ctx.getCorus().getConfigFacade().addProperty(scope, name, value, categorySet(ctx), getClusterInfo(ctx));
        } else {
          throw new InputException("Invalid property format; expected: <name>=<value>");
        }
      }
    } else if (op == Op.MERGE) {
      Properties processProps = new Properties();
      Results<List<Property>> propResults = ctx.getCorus().getConfigFacade().getAllProperties(PropertyScope.PROCESS, new ClusterInfo(false));
      for (Result<List<Property>> r : propResults) {
        for (Property p : r.getData()) {
          processProps.setProperty(p.getName(), p.getValue());
        }
      }
      
      File baseFile   = ctx.getFileSystem().getFile(ctx.getCommandLine().assertOption(OPT_BASE.getName(), true).getValue());
      File targetFile = ctx.getFileSystem().getFile(ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue());
      
      boolean replace = ctx.getCommandLine().containsOption(OPT_REPLACE.getName(), false);
      
      if (!baseFile.exists()) {
        throw new InputException("File does not exist: " + baseFile.getAbsolutePath());
      }
      
      Properties  mergedProperties = new Properties();
      Properties  baseProperties   = new Properties();
      InputStream is               = new FileInputStream(baseFile);
      try {
        baseProperties.load(is);
      } finally {
        is.close();
      }

      // adding base properties first
      StrSubstitutor substitutor = new StrSubstitutor(
          new CompositeStrLookup()
            .add(PropertiesStrLookup.getInstance(processProps))
            .add(ctx.getVars())
            .add(PropertiesStrLookup.systemPropertiesLookup())
            .lenient()
       );
      
      for (String n : baseProperties.stringPropertyNames()) {
        String value = baseProperties.getProperty(n);
        if (replace) {
          if (value != null) {
            value = substitutor.replace(value);
          }
        }
        mergedProperties.setProperty(n, value);
      }

      // overwriting base properties with process properties.
      for (String n : processProps.stringPropertyNames()) {
        mergedProperties.setProperty(n, processProps.getProperty(n));
      }
      
      FileOutputStream os = new FileOutputStream(targetFile);
      try {
        mergedProperties.store(os, "");
      } finally {
        os.flush();
        os.close();
      }
    } else if (op == Op.DELETE) {
      if (ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()) {
        ctx.getCommandLine().assertNextArg(new String[] { ARG_ALL });
        ctx.getCorus().getConfigFacade().removeProperty(
            scope, 
            ArgFactory.any(), 
            Collects.arrayToSet(ArgFactory.any()), 
            getClusterInfo(ctx)
        );
        ctx.getCorus().getConfigFacade().removeProperty(
            scope, 
            ArgFactory.any(), 
            new HashSet<Arg>(0), 
            getClusterInfo(ctx)
        );
      } else {
        String name = ctx.getCommandLine().assertOption(OPT_PROPERTY.getName(), true).getValue();
        ctx.getCorus().getConfigFacade().removeProperty(
            scope, ArgFactory.parse(name), categoryArgSet(ctx), getClusterInfo(ctx)
        );
      }
    } else if (op == Op.LIST) {
      Results<List<Property>> results = ctx.getCorus().getConfigFacade().getAllProperties(scope, getClusterInfo(ctx));
      results = Sorting.sortList(results, Property.class, ctx.getSortSwitches());
      displayPropertyResults(results, ctx);
    } else if (op == Op.EXPORT) {
      Results<List<Property>> results = ctx.getCorus().getConfigFacade().getAllProperties(scope, getClusterInfo(ctx));
      exportPropertyResults(results, ctx);
    } else if (op == Op.RENAME) {
      throw new InputException("Rename option not supported for properties");
    }
  }

  private void exportTagResults(Results<Set<Tag>> res, CliContext ctx) throws InputException {
    File exportFile = ctx.getFileSystem().getFile(ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue());
    try {
      PrintWriter writer = new PrintWriter(new FileOutputStream(exportFile));
      try {
        while (res.hasNext()) {
          Result<Set<Tag>> result = res.next();
          for (Tag tag : result.getData()) {
            writer.println(tag.getValue());
          }
        }
      } finally {
        writer.flush();
        writer.close();
      }
      ctx.getConsole().println("Tags exported to: " + exportFile.getAbsolutePath());
    } catch (IOException e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }

  }

  private void displayTagResults(Results<List<Tag>> res, CliContext ctx) throws InputException {
    String nameFilter = getOptValue(ctx, OPT_TAG.getName());
    if (nameFilter != null) {
      final org.sapia.corus.client.common.Arg pattern = ArgFactory.parse(nameFilter);
      res = res.filter(new Func<List<Tag>, List<Tag>>() {
        @Override
        public List<Tag> call(List<Tag> toFilter) {
          return Collects.filterAsList(toFilter, new Condition<Tag>() {
            @Override
            public boolean apply(Tag item) {
              return pattern.matches(item.getValue());
            }
          });
        }
      });
    }

    while (res.hasNext()) {
      Result<List<Tag>> result = res.next();
      displayTagsHeader(result.getOrigin(), ctx);
      for (Tag tag : result.getData()) {
        displayTag(tag, ctx);
      }
    }
  }

  private void displayTagsHeader(CorusHost addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = TAGS_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();

    headers.getCellAt(TAGS_TBL.col("val").index()).append("Tag");
    headers.flush();
  }

  private void displayTag(Tag tag, CliContext ctx) {
    Table tagsTable = TAGS_TBL.createTable(ctx.getConsole().out());

    tagsTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = tagsTable.newRow();
    row.getCellAt(TAGS_TBL.col("val").index()).append(tag.getValue());
    row.flush();
  }

  // --------------------------------------------------------------------------

  private void exportPropertyResults(Results<List<Property>> res, CliContext ctx) throws InputException {
    File exportFile = ctx.getFileSystem().getFile(ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue());
    try {
      PrintWriter writer = new PrintWriter(new FileOutputStream(exportFile));
      try {
        while (res.hasNext()) {
          Result<List<Property>> result = res.next();
          for (Property props : result.getData()) {
            writer.println(props.getName() + "=" + props.getValue());
          }
        }
      } finally {
        writer.flush();
        writer.close();
      }
      ctx.getConsole().println("Properties exported to: " + exportFile.getAbsolutePath());
    } catch (IOException e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }

  }

  private void displayPropertyResults(Results<List<Property>> res, CliContext ctx) throws InputException {
    String nameFilter = getOptValue(ctx, OPT_PROPERTY.getName());
    if (nameFilter == null) {
      if (ctx.getCommandLine().size() > 0) {
        CmdElement element = ctx.getCommandLine().get(ctx.getCommandLine().size() - 1);
        if (element instanceof Option) {
          int index = ctx.getCommandLine().size() - 2;
          if (index > 0) {
            element = ctx.getCommandLine().get(index);
          }
        }
        if (element instanceof org.sapia.console.Arg) {
          String argValue = ((org.sapia.console.Arg) element).getName().trim();
          if (!argValue.equals(ARG_LS)) {
            nameFilter = argValue;
          }
        }
      }
    }
    if (nameFilter != null) {
      final org.sapia.corus.client.common.Arg pattern = ArgFactory.parse(nameFilter);
      res = res.filter(new Func<List<Property>, List<Property>>() {
        @Override
        public List<Property> call(List<Property> toFilter) {
          return Collects.filterAsList(toFilter, new Condition<Property>() {
            @Override
            public boolean apply(Property item) {
              return pattern.matches(item.getName());
            }
          });
        }
      });
    }

    while (res.hasNext()) {
      Result<List<Property>> result = res.next();
      displayPropertiesHeader(result.getOrigin(), ctx);
      for (Property prop : result.getData()) {
        displayProperties(prop, ctx);
      }
    }
  }

  private void displayPropertiesHeader(CorusHost addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = PROPS_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();

    headers.getCellAt(PROPS_TBL.col("name").index()).append("Name");
    headers.getCellAt(PROPS_TBL.col("val").index()).append("Value");
    headers.getCellAt(PROPS_TBL.col("cat").index()).append("Category");

    headers.flush();
  }

  private void displayProperties(Property prop, CliContext ctx) {
    Table propsTable = PROPS_TBL.createTable(ctx.getConsole().out());

    propsTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = propsTable.newRow();
    row.getCellAt(PROPS_TBL.col("name").index()).append(prop.getName());
    if (prop.getName().contains(PASSWORD_PATTERN)) {
      row.getCellAt(PROPS_TBL.col("val").index()).append(PASSWORD_OBFUSCATION);
    } else {
      row.getCellAt(PROPS_TBL.col("val").index()).append(prop.getValue());
    }
    row.getCellAt(PROPS_TBL.col("cat").index()).append(prop.getCategory().isNull() ? "N/A" : prop.getCategory().get());
    row.flush();
  }
  
  private Set<Arg> categoryArgSet(CliContext ctx) {
    if (ctx.getCommandLine().containsOption(OPT_CATEGORY.getName(), true)) {
      List<String> toTransform =  Collects.arrayToList(
          StringUtils.split(ctx.getCommandLine().getOpt(OPT_CATEGORY.getName()).getValue(), ",")
      );
      return Collects.convertAsSet(toTransform, new Func<Arg, String>() {
        @Override
        public Arg call(String s) {
          return ArgFactory.parse(s);
        }
      });
    }
    return new HashSet<>(0);
  }

  private Set<String> categorySet(CliContext ctx) {
    if (ctx.getCommandLine().containsOption(OPT_CATEGORY.getName(), true)) {
      return Collects.arrayToSet(
          StringUtils.split(ctx.getCommandLine().getOpt(OPT_CATEGORY.getName()).getValue(), ",")
      );
    }
    return new HashSet<>(0);
  }
}
