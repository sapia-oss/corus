package org.sapia.corus.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.cloud.CorusUserData.Artifact;
import org.sapia.ubik.util.Strings;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Parses a Corus-compliant user data representation out of a JSON stream.
 * 
 * @author yduchesne
 * 
 */
class CorusUserDataParser {

  private CorusUserDataParser() {
  }

  /**
   * @param is
   *          the user data {@link InputStream}.
   * @return the {@link CorusUserData} that was parsed.
   * @throws IOException
   *           if an IO-related problem occurred while parsing.
   */
  static CorusUserData parse(InputStream is) throws IOException {

    CorusUserData ud = new CorusUserData();
    String content   = IOUtil.textStreamToString(is);
    JSONObject json  = JSONObject.fromObject(content);
    JSONObject corus = json.optJSONObject("corus");
    
    if (corus != null && !corus.isNullObject()) {

      String domain = corus.optString("domain");
      if (!Strings.isBlank(domain)) {
        ud.setDomain(domain.trim());
      }
      
      String role  = corus.optString("repo-role");
      if (!Strings.isBlank(role)) {
        ud.setRepoRole(RepoRole.valueOf(role.trim().toUpperCase()));
      }
      
      JSONArray jsonArtifacts = corus.optJSONArray("artifacts");
      if (jsonArtifacts != null) {
        for (int i = 0; i < jsonArtifacts.size(); i++) {
          JSONObject jsonArtifact = jsonArtifacts.getJSONObject(i);
          String url = jsonArtifact.getString("url");
          Artifact artifact = new Artifact(url);
          ud.getArtifacts().add(artifact);
        }
      }

      // server tags and properties
      JSONObject server = corus.optJSONObject("server");
      if (server != null && !server.isNullObject()) {
        populateProperties(ud.getServerProperties(), server.optJSONArray("properties"));

        JSONArray tags = server.optJSONArray("tags");
        if (tags != null) {
          for (int i = 0; i < tags.size(); i++) {
            String tag = tags.getString(i);
            ud.getServerTags().add(tag);
          }
        }
      }

      // process properties
      JSONObject processes = corus.optJSONObject("processes");
      if (processes != null && !processes.isNullObject()) {
        populateProperties(ud.getProcessProperties(), processes.optJSONArray("properties"));
      }
    }
    return ud;
  }

  private static void populateProperties(Properties toPopulate, JSONArray props) {
    if (props != null) {
      for (int i = 0; i < props.size(); i++) {
        JSONObject prop = props.getJSONObject(i);
        String name = prop.getString("name");
        String value = prop.getString("value");
        if (name != null && value != null) {
          toPopulate.setProperty(name, value);
        }
      }
    }
  }
}
