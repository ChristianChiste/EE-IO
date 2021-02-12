package at.uibk.dps.ee.io.json;

import java.util.List;

/**
 * Each {@link FunctionTypeEntry} contains the information about the resources
 * that functions of a certain type can be enacted on.
 * 
 * @author Fedor Smirnov
 */
public class FunctionTypeEntry {

  protected String functionType;
  protected List<ResourceEntry> resources;

  public FunctionTypeEntry(String functionType, List<ResourceEntry> resources) {
    this.functionType = functionType;
    this.resources = resources;
  }

  public String getFunctionType() {
    return functionType;
  }

  public void setFunctionType(String functionType) {
    this.functionType = functionType;
  }

  public List<ResourceEntry> getResources() {
    return resources;
  }

  public void setResources(List<ResourceEntry> resources) {
    this.resources = resources;
  }
}
