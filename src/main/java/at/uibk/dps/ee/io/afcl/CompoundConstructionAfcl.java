package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOutsAtomic;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionServerless;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * Static method container for the construction of the graph structures modeling
 * different AFCL compound.
 * 
 * @author Fedor Smirnov
 *
 */
public final class CompoundConstructionAfcl {

	/**
	 * No constructor.
	 */
	private CompoundConstructionAfcl() {
	}

	/**
	 * Adds and annotates the elements to the graph which model the given function.
	 * 
	 * @param graph    the graph to annotate
	 * @param function the function to model
	 */
	public static void addFunctionCompound(EnactmentGraph graph, Function function) {
		switch (UtilsAfcl.getCompoundType(function)) {
		case Atomic: {
			addAtomicFunctionWfLevel(graph, (AtomicFunction) function);
			return;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + UtilsAfcl.getCompoundType(function));
		}
	}

	/**
	 * Adds the elements of the provided atomic function to the graph. This method
	 * assumes that we are on the WF level (the sources of the functions can be
	 * directly mapped to nodes).
	 * 
	 * @param graph      the graph
	 * @param atomicFunc the provided atomic function
	 */
	protected static void addAtomicFunctionWfLevel(EnactmentGraph graph, AtomicFunction atomicFunc) {
		Task atomicTask = createTaskFromAtomicFunction(atomicFunc);
		// process the inputs
		for (DataIns dataIn : AfclApiWrapper.getDataIns(atomicFunc)) {
			addDataIn(graph, atomicTask, dataIn);
		}
		// process the outputs
		for (DataOutsAtomic dataOut : AfclApiWrapper.getDataOuts(atomicFunc)) {
			addDataOut(graph, atomicTask, dataOut);
		}
	}

	/**
	 * Processes the given data out by adding an edge (if data already in graph) or
	 * an edge and a data node (if data not yet in graph).
	 * 
	 * @param graph    the enactment graph
	 * @param function the node modeling the atomic function with given data out
	 * @param dataOut  the given data out
	 */
	protected static void addDataOut(final EnactmentGraph graph, final Task function, final DataOutsAtomic dataOut) {
		final String functionName = function.getId();
		final String jsonKey = AfclApiWrapper.getName(dataOut);
		final String dataNodeId = UtilsAfcl.getDataNodeId(functionName, jsonKey);
		final DataType dataType = UtilsAfcl.getDataTypeForString(dataOut.getType());
		// retrieve or create the data node
		final Task dataNodeOut = assureDataNodePresence(dataNodeId, dataType, graph);
		// create, annotate, and add the dependency to the graph
		final Dependency dependency = PropertyServiceDependency.createDependency(function, dataNodeOut);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, function, dataNodeOut, EdgeType.DIRECTED);
	}

	/**
	 * Processes the given data in by adding an edge (if data already in graph) or
	 * an edge and a data node (if data not yet in graph) to the graph.
	 * 
	 * @param graph    the enactment graph
	 * @param function the node modeling the function with the given data in
	 * @param dataIn   the given data in
	 */
	protected static void addDataIn(EnactmentGraph graph, Task function, DataIns dataIn) {
		String dataNodeId = AfclApiWrapper.getSource(dataIn);
		String jsonKey = AfclApiWrapper.getName(dataIn);
		DataType dataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
		// retrieve or create the data node
		Task dataNodeIn = assureDataNodePresence(dataNodeId, dataType, graph);
		// create annotate, and insert the edge
		Dependency dependency = PropertyServiceDependency.createDependency(dataNodeIn, function);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, dataNodeIn, function, EdgeType.DIRECTED);
	}

	/**
	 * If a node with the given id is not in the graph, creates the node. Otherwise
	 * checks that the node has the specified type. Returns the created/retrieved
	 * node.
	 * 
	 * @param dataNodeId the id of the data node
	 * @param dataType   the data type of the node
	 * @param graph      the enactment graph
	 * @return the created/retrieved node
	 */
	protected static Task assureDataNodePresence(String dataNodeId, DataType dataType, EnactmentGraph graph) {
		Task result = graph.getVertex(dataNodeId);
		if (result == null) {
			result = new Communication(dataNodeId);
			PropertyServiceData.setDataType(result, dataType);
		} else {
			if (!PropertyServiceData.getDataType(result).equals(dataType)) {
				throw new IllegalStateException("The type specified by node " + dataNodeId
						+ " does not match the type expected by a requestor/producer");
			}
		}
		return result;
	}

	/**
	 * Creates a task node from the given atomic function.
	 * 
	 * @param atomFunc the given atomic function
	 * @return the task node modeling the given atomic function.
	 */
	protected static Task createTaskFromAtomicFunction(AtomicFunction atomFunc) {
		String funcId = atomFunc.getName();
		Task result = new Task(funcId);
		String functionTypeString = atomFunc.getType();
		FunctionType funcType = UtilsAfcl.getFunctionTypeForString(functionTypeString);
		PropertyServiceFunction.setType(funcType, result);
		if (funcType.equals(FunctionType.Serverless)) {
			if (UtilsAfcl.isResourceSetAtomFunc(atomFunc)) {
				PropertyServiceFunctionServerless.setResource(result, UtilsAfcl.getResLinkAtomicFunction(atomFunc));
			}
		} else if (funcType.equals(FunctionType.Local)) {
			// Nothing special to do here
		} else {
			throw new IllegalArgumentException("Function type " + funcType.name() + " not yet covered.");
		}
		return result;
	}
}
