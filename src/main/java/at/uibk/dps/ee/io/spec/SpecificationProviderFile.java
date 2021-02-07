package at.uibk.dps.ee.io.spec;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import at.uibk.dps.ee.io.json.ResourceEntry;
import at.uibk.dps.ee.io.json.ResourceInformationJsonFile;
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

	@Inject
	public SpecificationProviderFile(EnactmentGraphProvider enactmentGraphProvider,
			ResourceGraphProvider resourceGraphProvider, String filePath) {
		this.enactmentGraphProvider = enactmentGraphProvider;
		this.resourceGraphProvider = resourceGraphProvider;
		this.mappings = createMappings(getEnactmentGraph(), getResourceGraph(), filePath);
		this.specification = new EnactmentSpecification(getEnactmentGraph(), getResourceGraph(), getMappings());
	}

	@Override
	public ResourceGraph getResourceGraph() {
		return resourceGraphProvider.getResourceGraph();
	}

	@Override
	public EnactmentGraph getEnactmentGraph() {
		return enactmentGraphProvider.getEnactmentGraph();
	}

	@Override
	public Mappings<Task, Resource> getMappings() {
		return mappings;
	}

	/**
	 * Reads the json file with the file information and uses it to create the
	 * mappings.
	 * 
	 * @param eGraph   the enactment graph
	 * @param rGraph   the resource graph
	 * @param filePath the file path to the resource information
	 * @return the mappings connected the eGraph and the rGraph
	 */
	protected Mappings<Task, Resource> createMappings(EnactmentGraph eGraph, ResourceGraph rGraph, String filePath) {
		Mappings<Task, Resource> result = new Mappings<>();
		ResourceInformationJsonFile resInfo = ResourceInformationJsonFile.readFromFile(filePath);
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
	 * @param task    the provided task
	 * @param resInfo the given resource information.
	 * @param rGraph  the resource graph
	 * @return the mappings for the provided task based on the given resource
	 *         information
	 */
	protected Set<Mapping<Task, Resource>> getMappingsForTask(Task task, ResourceInformationJsonFile resInfo,
			ResourceGraph rGraph) {
		String funcTypeString = PropertyServiceFunctionUser.getFunctionType(task);
		return resInfo.stream().filter(functionEntry -> funcTypeString.equals(functionEntry.getFunctionType()))
				.flatMap(functionEntry -> functionEntry.getResources().stream())
				.map(resEntry -> getResourceForResourceEntry(rGraph, resEntry))
				.map(res -> PropertyServiceMapping.createMapping(task, res)).collect(Collectors.toSet());
	}

	/**
	 * Gets the resource node matching the provided resource entry
	 * 
	 * @param rGraph   the resource graph
	 * @param resEntry the resource entry
	 * @return the resource node matching the provided resource entry
	 */
	protected Resource getResourceForResourceEntry(ResourceGraph rGraph, ResourceEntry resEntry) {
		Optional<Resource> result;
		if (resEntry.getType().equals(ResourceType.Local.name())) {
			// Resource is local EE
			result = Optional.ofNullable(rGraph.getVertex(ConstantsEEModel.idLocalResource));
		} else if (resEntry.getType().equals(ResourceType.Serverless.name())) {
			// Serverless resource => look for the Uri
			if (!resEntry.getProperties().containsKey(PropertyServiceResourceServerless.propNameUri)) {
				throw new IllegalArgumentException("No Uri annotated for serverless resource");
			}
			String uri = resEntry.getProperties().get(PropertyServiceResourceServerless.propNameUri).getAsString();
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