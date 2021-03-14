package at.uibk.dps.ee.io.spec;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.opt4j.core.start.Constant;
import com.google.inject.Inject;

import at.uibk.dps.ee.io.json.ResourceEntry;
import at.uibk.dps.ee.io.json.ResourceInformationJsonFile;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceResource;
import at.uibk.dps.ee.model.properties.PropertyServiceResource.ResourceType;
import at.uibk.dps.ee.model.properties.PropertyServiceResourceServerless;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * The {@link SpecificationProviderFile} creates the specification by taking the
 * enactment and the resource graph and connecting them using the mappings it
 * creates based on the resource description file.
 * 
 * @author Fedor Smirnov
 *
 */
public class SpecificationProviderFile implements SpecificationProvider {

  protected final EnactmentGraphProvider enactmentGraphProvider;
  protected final ResourceGraphProvider resourceGraphProvider;
  protected final Mappings<Task, Resource> mappings;
  protected final EnactmentSpecification specification;

  /**
   * Injection constructor.
   * 
   * @param enactmentGraphProvider class providing the {@link EnactmentGraph}
   * @param resourceGraphProvider class providing the {@link ResourceGraph}
   * @param filePath path to the file describing the functionType-to-resource
   *        relations
   */
  @Inject
  public SpecificationProviderFile(final EnactmentGraphProvider enactmentGraphProvider,
      final ResourceGraphProvider resourceGraphProvider, @Constant(value = "filePath",
          namespace = ResourceGraphProviderFile.class) final String filePath) {
    this.enactmentGraphProvider = enactmentGraphProvider;
    this.resourceGraphProvider = resourceGraphProvider;
    this.mappings = createMappings(getEnactmentGraph(), getResourceGraph(), filePath);
    this.specification =
        new EnactmentSpecification(getEnactmentGraph(), getResourceGraph(), getMappings());
  }

  @Override
  public final ResourceGraph getResourceGraph() {
    return resourceGraphProvider.getResourceGraph();
  }

  @Override
  public final EnactmentGraph getEnactmentGraph() {
    return enactmentGraphProvider.getEnactmentGraph();
  }

  @Override
  public final Mappings<Task, Resource> getMappings() {
    return mappings;
  }

  /**
   * Reads the json file with the file information and uses it to create the
   * mappings.
   * 
   * @param eGraph the enactment graph
   * @param rGraph the resource graph
   * @param filePath the file path to the resource information
   * @return the mappings connected the eGraph and the rGraph
   */
  protected final Mappings<Task, Resource> createMappings(final EnactmentGraph eGraph,
      final ResourceGraph rGraph, final String filePath) {
    final Mappings<Task, Resource> result = new Mappings<>();
    final ResourceInformationJsonFile resInfo = ResourceInformationJsonFile.readFromFile(filePath);
    eGraph.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task))
        .filter(task -> PropertyServiceFunction.getUsageType(task).equals(UsageType.User))
        .flatMap(task -> getMappingsForTask(task, resInfo, rGraph).stream())
        .forEach(mapping -> result.add(mapping));
    return result;
  }

  /**
   * Creates the mappings for the provided task based on the given resource
   * information.
   * 
   * @param task the provided task
   * @param resInfo the given resource information.
   * @param rGraph the resource graph
   * @return the mappings for the provided task based on the given resource
   *         information
   */
  protected Set<Mapping<Task, Resource>> getMappingsForTask(final Task task,
      final ResourceInformationJsonFile resInfo, final ResourceGraph rGraph) {
    final String funcTypeString = PropertyServiceFunctionUser.getFunctionTypeString(task);
    return resInfo.stream()
        .filter(functionEntry -> funcTypeString.equals(functionEntry.getFunctionType()))
        .flatMap(functionEntry -> functionEntry.getResources().stream())
        .map(resEntry -> getResourceForResourceEntry(rGraph, resEntry))
        .map(res -> PropertyServiceMapping.createMapping(task, res)).collect(Collectors.toSet());
  }

  /**
   * Gets the resource node matching the provided resource entry
   * 
   * @param rGraph the resource graph
   * @param resEntry the resource entry
   * @return the resource node matching the provided resource entry
   */
  protected Resource getResourceForResourceEntry(final ResourceGraph rGraph,
      final ResourceEntry resEntry) {
    Optional<Resource> result;
    if (resEntry.getType().equals(ResourceType.Local.name())) {
      // Resource is local EE
      result = Optional.ofNullable(rGraph.getVertex(ConstantsEEModel.idLocalResource));
    } else if (resEntry.getType().equals(ResourceType.Serverless.name())) {
      // Serverless resource => look for the Uri
      if (!resEntry.getProperties().containsKey(PropertyServiceResourceServerless.propNameUri)) {
        throw new IllegalArgumentException("No Uri annotated for serverless resource");
      }
      final String uri =
          resEntry.getProperties().get(PropertyServiceResourceServerless.propNameUri).getAsString();
      result = Optional.ofNullable(rGraph.getVertex(uri));
    } else {
      throw new IllegalArgumentException("Unknown resource type: " + resEntry.getType());
    }
    return result.orElseThrow();
  }

  @Override
  public EnactmentSpecification getSpecification() {
    return specification;
  }
}
