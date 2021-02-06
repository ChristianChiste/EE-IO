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

	public ResourceEntry(String type, Map<String, JsonElement> properties) {
		this.type = type;
		this.properties = properties;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, JsonElement> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, JsonElement> properties) {
		this.properties = properties;
	}
}
