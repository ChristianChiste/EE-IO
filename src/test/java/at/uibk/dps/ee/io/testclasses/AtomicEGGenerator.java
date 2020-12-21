package at.uibk.dps.ee.io.testclasses;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import edu.uci.ics.jung.graph.util.EdgeType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * Generates a simple {@link EnactmentGraph} with a single atomic function.
 * 
 * @author Fedor Smirnov
 *
 */
public class AtomicEGGenerator {

	/**
	 * Generates and returns an {@link EnactmentGraph} with a single atomic function.
	 * 
	 * @return an {@link EnactmentGraph} with a single atomic function
	 */
	public static EnactmentGraph generateGraph() {
		
		EnactmentGraph result = new EnactmentGraph();
		
		// input node
		Task input = new Communication("input");
		PropertyServiceData.setDataType(input, DataType.Number);
		PropertyServiceData.makeRoot(input);
		PropertyServiceData.setJsonKey(input, "wfInputKey");
		
		// function node
		Task atomic = new Task("atomic");
		PropertyServiceFunction.setResource(atomic, "myResource");
		
		// output node
		Task output = new Communication("output");
		PropertyServiceData.setDataType(output, DataType.String);
		PropertyServiceData.makeLeaf(output);
		PropertyServiceData.setJsonKey(output, "wfOutputKey");
		
		// connection input atomic
		Dependency dep0 = PropertyServiceDependency.createDependency(input, atomic);
		PropertyServiceDependency.setType(dep0, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dep0, "atomicInputKey");
		result.addEdge(dep0, input, atomic, EdgeType.DIRECTED);
		
		// connection atomic output
		Dependency dep1 = PropertyServiceDependency.createDependency(atomic, output);
		PropertyServiceDependency.setType(dep1, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dep1, "atomicOutputKey");
		result.addEdge(dep1, atomic, output, EdgeType.DIRECTED);
		
		return result;
	}
}
