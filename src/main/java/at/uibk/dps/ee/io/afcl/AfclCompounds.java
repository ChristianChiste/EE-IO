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
import at.uibk.dps.afcl.functions.objects.PropertyConstraint;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.constants.ConstantsEEModel.EIdxParameters;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityElementIndex;
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
		// create/retrieve the data node
		final String dataNodeId = AfclApiWrapper.getSource(dataIn);
		final String srcFunc = UtilsAfcl.getProducerId(dataNodeId);
		if (srcFunc.equals(function.getId())) {
			throw new IllegalStateException("Function " + function.getId() + " depends on itself.");
		}
		final String jsonKey = AfclApiWrapper.getName(dataIn);
		DataType dataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
		// retrieve or create the data node
		final Task dataNodeIn = assureDataNodePresence(dataNodeId, dataType, graph);

		if (isElementIndexDataIn(dataIn)) {
			dataType = DataType.Collection;
			String subCollectionString = getElementIndexValue(dataIn);
			String subCollectionDataId = dataNodeId + ConstantsEEModel.DependencyAffix + subCollectionString;
			// create the data node for the processed data
			DataType processedDataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
			if (!UtilsAfcl.doesElementIdxValueMapToOneValue(subCollectionString)
					&& !processedDataType.equals(DataType.Collection)) {
				throw new IllegalStateException("Processing the src " + srcFunc + " with the elementIdx string "
						+ subCollectionDataId + " will result in a collection, not in a  " + processedDataType.name());
			}
			final Task processedDataNode = assureDataNodePresence(subCollectionDataId, processedDataType, graph);
			final Task processingNode = processEIdxAfclString(subCollectionString, dataNodeId, graph);
			// connect the nodes
			// raw data to idx function
			final Dependency dependency1 = PropertyServiceDependency.createDataDependency(dataNodeIn, processingNode,
					jsonKey);
			graph.addEdge(dependency1, dataNodeIn, processingNode, EdgeType.DIRECTED);
			final Dependency dependency2 = PropertyServiceDependency.createDataDependency(processingNode,
					processedDataNode, jsonKey);
			graph.addEdge(dependency2, processingNode, processedDataNode, EdgeType.DIRECTED);
			final Dependency dependency3 = PropertyServiceDependency.createDataDependency(processedDataNode, function,
					jsonKey);
			graph.addEdge(dependency3, processedDataNode, function, EdgeType.DIRECTED);
		} else {
			// create annotate, and insert the edge (default case)
			final Dependency dependency = PropertyServiceDependency.createDataDependency(dataNodeIn, function, jsonKey);
			graph.addEdge(dependency, dataNodeIn, function, EdgeType.DIRECTED);
		}
	}

	/**
	 * Processes the given afcl string describing an element index relation. Returns
	 * the node representing the processing function after establishing all graph
	 * connections to the EIdx inputs (e.g. index or stride).
	 * 
	 * @param afclEIdxString     the afcl string describing the element index
	 * @param dataProcessingNode the node which will process the collection
	 * @param graph              the enactment graph
	 * @return the node representing the processing function after establishing all
	 *         graph connections
	 */
	protected static Task processEIdxAfclString(final String afclEIdxString, final String dataNodeId,
			final EnactmentGraph graph) {
		final String subCollString = UtilsAfcl.generateEidxString(afclEIdxString);
		final Task result = PropertyServiceFunctionUtilityElementIndex.createElementIndexTask(dataNodeId,
				subCollString);
		// iterate through the string. Make a connection every time we see a source.
		if (afclEIdxString.contains(ConstantsEEModel.EIdxSeparatorExternal)) {
			// more than one element
			final String[] substrings = afclEIdxString.split(ConstantsEEModel.EIdxSeparatorExternal);
			for (int idx = 0; idx < substrings.length; idx++) {
				final String substring = substrings[idx];
				processEIdxAfclSubString(substring, result, graph, idx);
			}
		} else {
			processEIdxAfclSubString(afclEIdxString, result, graph, 0);
		}
		return result;
	}

	/**
	 * If necessary, establish the node connections for the given substring (if e.g.
	 * the stride is defined by a data out).
	 * 
	 * @param subString    the substring
	 * @param functionNode the processing node
	 * @param graph        the enactment graph
	 * 
	 */
	protected static void processEIdxAfclSubString(String subString, Task functionNode, EnactmentGraph graph, int idx) {
		if (subString.contains(ConstantsEEModel.EIdxSeparatorInternal)) {
			// start end stride
			String[] subSubstrings = subString.split(ConstantsEEModel.EIdxSeparatorInternal);
			for (int idxIdx = 0; idxIdx < subSubstrings.length; idxIdx++) {
				String subSubString = subSubstrings[idxIdx];
				if (UtilsAfcl.isSrcString(subSubString)) {
					Task inputNode = assureDataNodePresence(subSubString, DataType.Number, graph);
					EIdxParameters params = null;
					if (idxIdx == 0) {
						params = EIdxParameters.Start;
					} else if (idxIdx == 1) {
						params = EIdxParameters.End;
					} else {
						params = EIdxParameters.Stride;
					}
					connectEidxInput(functionNode, inputNode, params, graph, idx);
				}
			}
		} else {
			// index
			if (UtilsAfcl.isSrcString(subString)) {
				EIdxParameters params = EIdxParameters.Index;
				Task inputNode = assureDataNodePresence(subString, DataType.Number, graph);
				connectEidxInput(functionNode, inputNode, params, graph, idx);
			}
		}
	}

	/**
	 * Connects the nodes to establish a connection between a param input and the
	 * EIdx function
	 * 
	 * @param function the EIDX function node
	 * @param data     the data node
	 * @param param    the type of paramenter
	 * @param graph    the enactment graph
	 * @param idx      the index within the EIdx string
	 */
	protected static void connectEidxInput(final Task function, final Task data, final EIdxParameters param,
			final EnactmentGraph graph, final int idx) {
		final String jsonKey = param.name() + ConstantsEEModel.EIdxEdgeIdxSeparator + idx;
		final Dependency dependency = PropertyServiceDependency.createDataDependency(data, function, jsonKey);
		graph.addEdge(dependency, data, function, EdgeType.DIRECTED);
	}

	/**
	 * Returns true if the given data in is annotated as an element index data in.
	 * 
	 * @param dataIn the given data in
	 * @return true if the given data in is annotated as an element index data in
	 */
	protected static boolean isElementIndexDataIn(final DataIns dataIn) {
		if (!AfclApiWrapper.hasConstraints(dataIn)) {
			return false;
		}
		for (PropertyConstraint constraint : dataIn.getConstraints()) {
			if (constraint.getName().equals(ConstantsAfcl.constraintNameElementIndex)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value string of the given data in. Throws an exception if no
	 * element index constraint is defined for the data in.
	 * 
	 * @param dataIn the given data in
	 * @return the values string of the data in
	 */
	protected static String getElementIndexValue(final DataIns dataIn) {
		if (!isElementIndexDataIn(dataIn)) {
			throw new IllegalArgumentException("The data in with the name " + AfclApiWrapper.getName(dataIn)
					+ " has no element index constraint.");
		}
		for (PropertyConstraint constraint : dataIn.getConstraints()) {
			if (constraint.getName().equals(ConstantsAfcl.constraintNameElementIndex)) {
				return constraint.getValue();
			}
		}
		// this line should never be reached
		throw new IllegalArgumentException("No value found for element index constraint.");
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
