package at.uibk.dps.ee.io.spec;

import com.google.inject.Inject;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;

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
	 * Reads the json file with the file information and uses it to create the mappings.
	 * 
	 * @param eGraph the enactment graph
	 * @param rGraph the resource graph
	 * @param filePath the file path to the resource information
	 * @return the mappings connected the eGraph and the rGraph
	 */
	protected Mappings<Task, Resource> createMappings(EnactmentGraph eGraph, ResourceGraph rGraph, String filePath){
		
	}
	
	@Override
	public EnactmentSpecification getSpecification() {
		return specification;
	}

}
