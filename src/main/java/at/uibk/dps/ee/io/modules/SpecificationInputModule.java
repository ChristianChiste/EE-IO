package at.uibk.dps.ee.io.modules;

import org.opt4j.core.config.annotations.File;
import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.start.Constant;
import at.uibk.dps.ee.guice.modules.InputModule;
import at.uibk.dps.ee.io.afcl.AfclReader;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.graph.SpecificationProvider;

/**
 * The {@link SpecificationInputModule} is used to read in the specification.
 * 
 * @author Fedor Smirnov
 *
 */
public class SpecificationInputModule extends InputModule {

  @Order(1)
  @Info("The path to the .afcl file.")
  @File
  @Constant(value = "filePath", namespace = AfclReader.class)
  public String filePathAfcl = "";

  @Order(2)
  @Info("The path to the file describing the function-type-to-resource mapping.")
  @File
  @Constant(value = "filePath", namespace = ResourceGraphProviderFile.class)
  public String filePathMappingFile = "";

  public String getFilePathAfcl() {
    return filePathAfcl;
  }

  public void setFilePathAfcl(String filePathAfcl) {
    this.filePathAfcl = filePathAfcl;
  }

  public String getFilePathMappingFile() {
    return filePathMappingFile;
  }

  public void setFilePathMappingFile(String filePathMappingFile) {
    this.filePathMappingFile = filePathMappingFile;
  }

  @Override
  protected void config() {
    bind(EnactmentGraphProvider.class).to(AfclReader.class);
    bind(ResourceGraphProvider.class).to(ResourceGraphProviderFile.class);
    bind(SpecificationProvider.class).to(SpecificationProviderFile.class);
  }
}
