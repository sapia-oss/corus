package org.sapia.corus.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sapia.corus.util.IOUtil;

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
   * @param is the user data {@link InputStream}.
   * @return the {@link CorusUserData} that was parsed.
   * @throws IOException if an IO-related problem occurred while parsing.
   */
  static CorusUserData parse(InputStream is) throws IOException {
    
    CorusUserData ud      = new CorusUserData();
    String        content = IOUtil.textStreamToString(is);
    JSONObject    json    = JSONObject.fromObject(content);
    JSONObject    corus   = json.getJSONObject("corus");
    
    if (corus != null && !corus.isNullObject()) {
      
      // server tags and properties
      JSONObject server = corus.getJSONObject("server");
      if (!server.isNullObject()) {
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
      JSONObject processes = corus.getJSONObject("processes");
      if (!processes.isNullObject()) {
         populateProperties(ud.getProcessProperties(), processes.getJSONArray("properties"));
      }
    }
    return ud;
  }
  
  private static void populateProperties(Properties toPopulate, JSONArray props)  {
    if (props != null) {
      for (int i = 0; i < props.size() ; i++) {
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
