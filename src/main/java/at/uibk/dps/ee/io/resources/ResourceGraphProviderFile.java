package at.uibk.dps.ee.io.resources;

import java.util.Optional;

import at.uibk.dps.ee.io.json.ResourceEntry;
import at.uibk.dps.ee.io.json.ResourceInformationJsonFile;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceLink;
import at.uibk.dps.ee.model.properties.PropertyServiceResource;
import at.uibk.dps.ee.model.properties.PropertyServiceResource.ResourceType;
import at.uibk.dps.ee.model.properties.PropertyServiceResourceServerless;
import net.sf.opendse.model.Resource;

/**
 * The {@link ResourceGraphProviderFile} provides access to the resource graph
 * which it builds from a .json file
 * 
 * @author Fedor Smirnov
 *
 */
public class ResourceGraphProviderFile implements ResourceGraphProvider {

	protected final ResourceGraph resourceGraph;

	public ResourceGraphProviderFile(String filePath) {
		this.resourceGraph = readResourceGraph(filePath);
	}

	@Override
	public ResourceGraph getResourceGraph() {
		return resourceGraph;
	}

	protected ResourceGraph readResourceGraph(String filePath) {
		ResourceGraph result = new ResourceGraph();
		ResourceInformationJsonFile resourceInformation = ResourceInformationJsonFile.readFromFile(filePath);
		// always add a node representing the EE
		Resource eeRes = PropertyServiceResource.createResource(ConstantsEEModel.idLocalResource, ResourceType.Local);
		resourceInformation.stream().flatMap(functionTypeEntry -> functionTypeEntry.getResources().stream())
				.forEach(resourceEntry -> processResourceEntry(result, eeRes, resourceEntry));
		return result;
	}

	/**
	 * Method to process a resource entry by adding a resource node to the graph and
	 * connecting it to the EE node.
	 * 
	 * @param resourceGraph the resource graph
	 * @param ee            the resource modeling the EE
	 * @param resEntry      the resource entry
	 */
	protected void processResourceEntry(ResourceGraph resourceGraph, Resource ee, ResourceEntry resEntry) {
		ResourceType resourceType = ResourceType.valueOf(resEntry.getType());
		Optional<Resource> newResourceOpt;
		if (resourceType.equals(ResourceType.Local)) {
			// nothing to do, EE already in the graph
			if (!resEntry.getProperties().isEmpty()) {
				throw new IllegalArgumentException("Entry of the EE resource should not have properties.");
			}
			return;
		} else if (resourceType.equals(ResourceType.Serverless)) {
			String uri = resEntry.getProperties().get(PropertyServiceResourceServerless.propNameUri).getAsString();
			newResourceOpt = Optional.of(PropertyServiceResourceServerless.createServerlessResource(uri, uri));

		} else {
			throw new IllegalArgumentException("Unknown resource type: " + resourceType.name());
		}
		Resource newRes = newResourceOpt.orElseThrow();
		// annotate all properties (if not already set)
		resEntry.getProperties().entrySet().stream()
				.filter(entry -> !newRes.getAttributeNames().contains(entry.getKey()))
				.forEach(entry -> newRes.setAttribute(entry.getKey(), entry.getValue()));
		// connect resource to ee node
		PropertyServiceLink.connectResources(resourceGraph, ee, newRes);
	}
}
