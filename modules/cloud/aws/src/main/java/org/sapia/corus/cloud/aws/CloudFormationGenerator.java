package org.sapia.corus.cloud.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Generates AWS cloud formation content by passing {@link Topology} instances
 * to underlying Freemarker templates.
 *  
 * @author yduchesne
 *
 */
public class CloudFormationGenerator {
  
  /**
   * A builder of {@link CloudFormationGenerator}s.
   * 
   * @author yduchesne
   *
   */
  public static final class Builder {
    
    private Configuration        templates  = new Configuration(Configuration.VERSION_2_3_22);
    private List<TemplateLoader> loaders    = new ArrayList<TemplateLoader>();
    private Map<String, String>  globalTags = new HashMap<String, String>();
    private Charset              charset    = Charsets.UTF_8;
    
    {
      loaders.add(new ClassTemplateLoader(CloudFormationGenerator.class, "/template"));
    }
    
    /**
     * @param templateConfig a template {@link Configuration}.
     * @return this instance.
     */
    public Builder withTemplateDir(File templateDir) {
      try {
        loaders.add(new FileTemplateLoader(templateDir));
      } catch (IOException e) {
        throw new IllegalStateException("Could not assign template directory: " + templateDir, e);
      }
      return this;
    }
    
    /**
     * @param loader the {@link TemplateLoader} to use.
     * @return this instance.
     */
    public Builder withTemplateLoader(TemplateLoader loader) {
      loaders.add(loader);
      return this;
    }
    
    /**
     * @param tags a {@link Map} of AWS tags with which to tag all the resources in the CloudFormation
     * that will be generated.
     * @return this instance.
     */
    public Builder withGlobalTags(Map<String, String> tags) {
      this.globalTags.putAll(tags);
      return this;
    }
    
    /**
     * @param charset the {@link Charset} to use when generating CloudFormation output.
     * @return this instance.
     */
    public Builder withCharset(Charset charset) {
      this.charset = charset;
      return this;
    }
    
    public CloudFormationGenerator build() {
      CloudFormationGenerator gen = new CloudFormationGenerator();
      gen.templates = templates;
      gen.templates.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()])));
      gen.globalTags = globalTags;
      gen.charset = charset;
      return gen;
    }
  }

  // ==========================================================================
  
  private Configuration        templates  = new Configuration(Configuration.VERSION_2_3_22);
  private Map<String, String>  globalTags = new HashMap<String, String>();
  private Charset              charset    = Charsets.UTF_8;

  private CloudFormationGenerator() {
  }
  
  /**
   * @param topo the {@link Topology} to process.
   * @param env the name of the environment for which to generate a Cloud Formation.
   * @param templatePath the path to the cloud formation template to use.
   * @return a cloud formation, as a JSON document.
   * @throws IOException if an I/O error occurs.
   * @throws TemplateException if a template processing problem occurs.
   */
  public String generateString(Topology topo, String env, String templatePath) throws IOException, TemplateException {
    Preconditions.checkNotNull(templates, "Template configuration not set");
    
    topo.render();
    topo.validate();
    
    Template template = templates.getTemplate(templatePath);
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("globalTags", globalTags);
    model.put("topology", topo);
    model.put("environment", topo.getEnvByName(env));
    StringWriter writer = new StringWriter();
    template.process(model, writer);
    String nonFormatted = writer.toString();
    return nonFormatted;
  }
  
  /**
   * @param topo the {@link Topology} to process.
   * @param env the name of the environment for which to generate a Cloud Formation.
   * @param templatePath the path to the cloud formation template to use.
   * @return a cloud formation, as a JSON document.
   * @throws IOException if an I/O error occurs.
   * @throws TemplateException if a template processing problem occurs.
   */
  public void generateFile(Topology topo, String env, String templatePath, File output) throws IOException, TemplateException {
    Files.write(generateString(topo, env, templatePath), output, charset);
  }
  
  public static void main(String[] args) throws Exception {
    File templateOut = new File("target/basic_topology.json");
    CloudFormationGenerator gen = new CloudFormationGenerator.Builder().build();
    gen.generateFile(Topology.newInstance(new File("etc/basic_topology.xml")), "dev", "/aws/cloud_formation.ftl", templateOut);
    
    CreateStackRequest req = new CreateStackRequest();
    req.setStackName("Corus-test");
    req.setOnFailure(OnFailure.DELETE);
    req.setGeneralProgressListener(new ProgressListener() {
      @Override
      public void progressChanged(ProgressEvent p) {
        System.out.println(p.getEventType().name());
      }
    });
    InputStreamReader reader = new InputStreamReader(new FileInputStream(templateOut));
    try {
      req.setTemplateBody(CharStreams.toString(reader));
    } finally {
      reader.close();
    }
    AmazonCloudFormationClient client = new AmazonCloudFormationClient();
    //client.createStack(req);
  }
  
}
