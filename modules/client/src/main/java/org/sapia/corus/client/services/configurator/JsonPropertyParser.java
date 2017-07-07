package org.sapia.corus.client.services.configurator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Assertions;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Parse the JSON property configuration of the following format:
 * 
 * <pre>
 *   {
 *      "categories": [ 
 *        {
 *          "name": "cat1"
 *          "properties": [
 *            {
 *              "name": "prop1", "value": "value1"
 *            },
 *            {
 *              "name": "prop2", "value": "value2"
 *            }
 *          ]
 *        }
 *      ],
 *      "properties": [
 *        {
 *          "name": "prop1", "value": "value1", categories: [ "cat1", "cat2" ]
 *        },
 *        {
 *          "name": "prop2", "value": "value2"
 *        }
 *      ]
 *   }
 * 
 * </pre>
 * 
 * @author yduchesne
 *
 */
public final class JsonPropertyParser {
  
  private JsonPropertyParser() {
    
  }

  /**
   * @param globalCategory an optional category to add to all the properties.
   * @param content  a JSON property string.
   * @param callback the {@link Consumer} to notify when a {@link Property} is parsed.
   */
  public static void parse(OptionalValue<String> globalCategory, String content, Consumer<Property> callback) {
    if (!content.isEmpty()) {
      JSONObject jsonConf = JSONObject.fromObject(content);
      if (jsonConf.has("categories")) {
        parseCategories(globalCategory, jsonConf, callback);
      }

      if (jsonConf.has("properties")) {
        parseProperties(globalCategory, jsonConf, callback);
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted

  private static void parseCategories(OptionalValue<String> globalCategory, JSONObject jsonConf, Consumer<Property> callback) {
    JSONArray jsonCategories = jsonConf.getJSONArray("categories");
    for (int i = 0; i < jsonCategories.size(); i++) {

      JSONObject jsonCategory = jsonCategories.getJSONObject(i);
      Assertions.isTrue(jsonCategory.has("name"), "JSON category object must have a 'name' field");
      Assertions.isTrue(jsonCategory.has("properties"), "JSON category object must have a 'properties' field");

      String category = jsonCategory.getString("name");
      JSONArray jsonProps = jsonCategory.getJSONArray("properties");
      for (int j = 0; j < jsonProps.size(); j++) {
        JSONObject jsonProp = jsonProps.getJSONObject(j);

        Assertions.isTrue(jsonProp.has("name"), "JSON property object must have a 'name' field");
        Assertions.isTrue(jsonProp.has("value"), "JSON property object must have a 'value' field");

        Property prop = new Property(jsonProp.getString("name"), jsonProp.getString("value"), category);
        callback.accept(prop);
        globalCategory.ifSet(gt -> callback.accept(prop.getCopyWith(gt)));
      }
    }
  }

  private static void parseProperties(OptionalValue<String> globalCategory, JSONObject jsonConf, Consumer<Property> callback) {
    JSONArray jsonProps = jsonConf.getJSONArray("properties");
    for (int i = 0; i < jsonProps.size(); i++) {
      JSONObject jsonProp = jsonProps.getJSONObject(i);
      Set<String> categories = new HashSet<>();

      if (jsonProp.has("categories")) {
        JSONArray jsonCategories = jsonProp.getJSONArray("categories");
        for (int j = 0; j < jsonCategories.size(); j++) {
          categories.add(jsonCategories.getString(j));
        }
      }
      Assertions.isTrue(jsonProp.has("name"), "JSON property object must have a 'name' field");
      Assertions.isTrue(jsonProp.has("value"), "JSON property object must have a 'value' field");

      if (categories.isEmpty()) {
        Property prop = new Property(jsonProp.getString("name"), jsonProp.getString("value"));
        globalCategory.ifSet(gt -> callback.accept(prop.getCopyWith(gt)));
        callback.accept(prop);
      } else {
        for (String c : categories) {
          Property prop = new Property(jsonProp.getString("name"), jsonProp.getString("value"), c);
          globalCategory.ifSet(gt -> callback.accept(prop.getCopyWith(gt)));
          callback.accept(prop);
        }
      }
    }
  }
}
