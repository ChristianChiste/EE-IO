package at.uibk.dps.ee.io.afcl;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.IfThenElse;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Static method container for the construction of the graph structures modeling
 * different AFCL compound.
 * 
 * @author Fedor Smirnov
 *
 */
public final class AfclCompounds {

	/**
	 * No constructor.
	 */
	private AfclCompounds() {
	}

	/**
	 * Adds and annotates the elements which model the given function to the graph.
	 * 
	 * @param graph    the graph to annotate
	 * @param function the function to model
	 * @param workflow the afcl workflow object
	 */
	public static void addFunctionCompound(final EnactmentGraph graph, final Function function,
			final Workflow workflow) {
		switch (UtilsAfcl.getCompoundType(function)) {
		case Atomic: {
			AfclCompoundsAtomic.addAtomicFunctionWfLevel(graph, (AtomicFunction) function);
			return;
		}
		case Sequence: {
			AfclCompoundsSequence.addSequence(graph, (Sequence) function, workflow);
			return;
		}
		case Parallel: {
			AfclCompoundsParallel.addParallel(graph, (Parallel) function, workflow);
			return;
		}
		case If: {
			AfclCompoundsIf.addIf(graph, (IfThenElse) function, workflow);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + UtilsAfcl.getCompoundType(function));
		}
	}

	/**
	 * Returns a set of all function nodes in the given graph.
	 * 
	 * @param graph the given graph
	 * @return a set of all function nodes in the given graph
	 */
	protected static Set<Task> getFunctionNodes(final EnactmentGraph graph) {
		final Set<Task> result = new HashSet<>();
		for (final Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				result.add(task);
			}
		}
		return result;
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
		final Dependency dependency = PropertyServiceDependency.createDataDependency(constantDataNode, function,
				jsonKey);
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
		final Dependency dependency = PropertyServiceDependency.createDataDependency(dataNodeIn, function, jsonKey);
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
}
