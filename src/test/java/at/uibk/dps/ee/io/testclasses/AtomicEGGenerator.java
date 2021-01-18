package at.uibk.dps.ee.io.testclasses;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionServerless;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

/**
 * Generates a simple {@link EnactmentGraph} with a single atomic function.
 * 
 * @author Fedor Smirnov
 *
 */
public class AtomicEGGenerator {

	/**
	 * Generates and returns an {@link EnactmentGraph} with a single atomic
	 * function.
	 * 
	 * @return an {@link EnactmentGraph} with a single atomic function
	 */
	public static EnactmentGraph generateGraph() {

		EnactmentGraph result = new EnactmentGraph();

		// input node
		Task input = new Communication("single Atomic/input_name");
		PropertyServiceData.setDataType(input, DataType.Number);
		PropertyServiceData.makeRoot(input);
		PropertyServiceData.setJsonKey(input, "inputSource");

		// function node
		Task atomic = new Task("atomicFunction");
		PropertyServiceFunction.setType(FunctionType.Serverless, atomic);
		PropertyServiceFunctionServerless.setResource(atomic, "my_res_link");

		// output node
		Task output = new Communication("atomicFunction/myOutput");
		PropertyServiceData.setDataType(output, DataType.String);
		PropertyServiceData.makeLeaf(output);
		PropertyServiceData.setJsonKey(output, "output_name");

		// connection input atomic
		PropertyServiceDependency.addDataDependency(input, atomic, "myInput", result);
		// connection atomic output
		PropertyServiceDependency.addDataDependency(atomic, output, "myOutput", result);
		return result;
	}
}
