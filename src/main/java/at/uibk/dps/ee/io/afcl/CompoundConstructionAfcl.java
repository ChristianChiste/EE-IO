package at.uibk.dps.ee.io.afcl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOutsAtomic;
import at.uibk.dps.afcl.functions.objects.Section;
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
	 * @param workflow the afcl workflow object
	 */
	public static void addFunctionCompound(final EnactmentGraph graph, final Function function,
			final Workflow workflow) {
		switch (UtilsAfcl.getCompoundType(function)) {
		case Atomic: {
			addAtomicFunctionWfLevel(graph, (AtomicFunction) function);
			return;
		}
		case Sequence: {
			addSequence(graph, (Sequence) function, workflow);
			return;
		}
		case Parallel: {
			addParallel(graph, (Parallel) function, workflow);
			return;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + UtilsAfcl.getCompoundType(function));
		}
	}

	/**
	 * Adds the nodes modeling the content of the given parallel compound to the
	 * provided enactment graph.
	 * 
	 * @param graph    the enactment graph
	 * @param parallel the parallel compound
	 * @param workflow the afcl workflow object
	 */
	protected static void addParallel(final EnactmentGraph graph, final Parallel parallel, final Workflow workflow) {
		for (final Section section : parallel.getParallelBody()) {
			for (final Function function : section.getSection()) {
				if (function instanceof AtomicFunction) {
					addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
				} else {
					addFunctionCompound(graph, function, workflow);
				}
			}
		}
	}

	/**
	 * Adds the nodes modeling the content of the given sequence compound to the
	 * provided enactment graph.
	 * 
	 * @param graph    the enactment graph
	 * @param sequence the provided sequence compound
	 * @param workflow the afcl workflow object
	 */
	protected static void addSequence(final EnactmentGraph graph, final Sequence sequence, final Workflow workflow) {
		for (final Function function : sequence.getSequenceBody()) {
			if (function instanceof AtomicFunction) {
				addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
			} else {
				addFunctionCompound(graph, function, workflow);
			}
		}
	}

	/**
	 * Corrects the data in of the given atomic function to point directly to the
	 * data input.
	 * 
	 * @param function the atomic function
	 * @param workflow the afcl workflow object
	 */
	protected static void correctAtomicDataIns(final AtomicFunction function, final Workflow workflow) {
		for (final DataIns dataIn : AfclApiWrapper.getDataIns(function)) {
			final String srcString = dataIn.getSource();
			final String actualSrc = HierarchyLevellingAfcl.getSrcDataId(srcString, workflow);
			dataIn.setSource(actualSrc);
		}
	}

	/**
	 * Adds the node modeling the given atomic function (which is described on a
	 * compound level, i.e., not the highest level of the workflow) to the enactment
	 * graph.
	 * 
	 * @param graph    the enactment graph
	 * @param atomic   the atomic function
	 * @param workflow the afcl workflow object
	 */
	protected static void addAtomicFunctionSubWfLevel(final EnactmentGraph graph, final AtomicFunction atomic,
			final Workflow workflow) {
		correctAtomicDataIns(atomic, workflow);
		addAtomicFunctionWfLevel(graph, atomic);
	}

	/**
	 * Adds the elements of the provided atomic function to the graph. This method
	 * assumes that we are on the WF level (the sources of the functions can be
	 * directly mapped to nodes).
	 * 
	 * @param graph      the graph
	 * @param atomicFunc the provided atomic function
	 */
	protected static void addAtomicFunctionWfLevel(final EnactmentGraph graph, final AtomicFunction atomicFunc) {
		final Task atomicTask = createTaskFromAtomicFunction(atomicFunc);
		// process the inputs
		for (final DataIns dataIn : AfclApiWrapper.getDataIns(atomicFunc)) {
			if (UtilsAfcl.isSrcString(AfclApiWrapper.getSource(dataIn))) {
				addDataInDefault(graph, atomicTask, dataIn);
			} else {
				addDataInConstant(graph, atomicTask, dataIn);
			}
		}
		// process the outputs
		for (final DataOutsAtomic dataOut : AfclApiWrapper.getDataOuts(atomicFunc)) {
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
	 * Processes the given dataIn representing constant data: generates a constant
	 * data node and connects it to the function node.
	 * 
	 * @param graph    the enactment graph
	 * @param function the node modeling the function with the given data in
	 * @param dataIn   the given data in (representing a constant input)
	 */
	protected static void addDataInConstant(final EnactmentGraph graph, final Task function, final DataIns dataIn) {
		final String jsonKey = AfclApiWrapper.getName(dataIn);
		final String dataNodeId = function.getId() + ConstantsAfcl.SourceAffix + jsonKey;
		final DataType dataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
		final String jsonString = AfclApiWrapper.getSource(dataIn);
		final JsonElement content = JsonParser.parseString(jsonString);

		final Task constantDataNode = PropertyServiceData.createConstantNode(dataNodeId, dataType, content);
		final Dependency dependency = PropertyServiceDependency.createDependency(constantDataNode, function);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, constantDataNode, function, EdgeType.DIRECTED);
	}

	/**
	 * Processes the given data in by adding an edge (if data already in graph) or
	 * an edge and a data node (if data not yet in graph) to the graph.
	 * 
	 * @param graph    the enactment graph
	 * @param function the node modeling the function with the given data in
	 * @param dataIn   the given data in
	 */
	protected static void addDataInDefault(final EnactmentGraph graph, final Task function, final DataIns dataIn) {
		final String dataNodeId = AfclApiWrapper.getSource(dataIn);
		final String srcFunc = UtilsAfcl.getProducerId(dataNodeId);
		if (srcFunc.equals(function.getId())) {
			throw new IllegalStateException("Function " + function.getId() + " depends on itself.");
		}
		final String jsonKey = AfclApiWrapper.getName(dataIn);
		final DataType dataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
		// retrieve or create the data node
		final Task dataNodeIn = assureDataNodePresence(dataNodeId, dataType, graph);
		// create annotate, and insert the edge
		final Dependency dependency = PropertyServiceDependency.createDependency(dataNodeIn, function);
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
	protected static Task assureDataNodePresence(final String dataNodeId, final DataType dataType,
			final EnactmentGraph graph) {
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
	protected static Task createTaskFromAtomicFunction(final AtomicFunction atomFunc) {
		final String funcId = atomFunc.getName();
		final Task result = new Task(funcId);
		final String functionTypeString = atomFunc.getType();
		final FunctionType funcType = UtilsAfcl.getFunctionTypeForString(functionTypeString);
		PropertyServiceFunction.setType(funcType, result);
		if (funcType.equals(FunctionType.Serverless)) {
			if (UtilsAfcl.isResourceSetAtomFunc(atomFunc)) {
				PropertyServiceFunctionServerless.setResource(result, UtilsAfcl.getResLinkAtomicFunction(atomFunc));
			}
		} else if (!funcType.equals(FunctionType.Local)) {
			throw new IllegalArgumentException("Function type " + funcType.name() + " not yet covered.");
		}
		return result;
	}
}
