package at.uibk.dps.ee.io.json;

import java.util.Map;

import com.google.gson.JsonElement;

/**
 * Each resource entry contains the information about a particular resource.
 * 
 * @author Fedor Smirnov
 */
public class ResourceEntry {

  protected String type;
  protected Map<String, JsonElement> properties;

  /**
   * Default constructor used by Gson.
   * 
   * @param type string describing the resource type
   * @param properties map of properties
   */
  public ResourceEntry(final String type, final Map<String, JsonElement> properties) {
    this.type = type;
    this.properties = properties;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public Map<String, JsonElement> getProperties() {
    return properties;
  }

  public void setProperties(final Map<String, JsonElement> properties) {
    this.properties = properties;
  }
}
