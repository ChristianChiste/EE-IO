package at.uibk.dps.ee.io.afcl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonPrimitive;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.ParallelFor;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOuts;
import at.uibk.dps.ee.io.UtilsIO;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections.OperationType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

/**
 * Static method container for the methods used when creating the enactment
 * graph parts modeling elements of sequence compounds.
 * 
 * @author Fedor Smirnov
 */
public final class AfclCompoundsParallelFor {

	/**
	 * No constructor.
	 */
	private AfclCompoundsParallelFor() {
	}

	/**
	 * Adds the nodes modeling the contents of the provided parallel for compound to
	 * the provided enactment graph
	 * 
	 * @param graph       the provided enactment graph
	 * @param parallelFor the parallelFor compound
	 * @param workflow    the afcl workflow
	 */
	protected static void addParallelFor(final EnactmentGraph graph, final ParallelFor parallelFor,
			final Workflow workflow) {

		// process the iterators and add the distribute function
		List<String> iterators = parallelFor.getIterators().stream().map(String::trim).collect(Collectors.toList());
		String distributionId = parallelFor.getName() + ConstantsEEModel.KeywordSeparator1
				+ ConstantsEEModel.FuncNameUtilityDistribution;

		Task distributionNode = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask(distributionId,
				OperationType.Distribution, parallelFor.getName());

		List<DataIns> dataIns = AfclApiWrapper.getDataIns(parallelFor);

		if (isIntIteratorList(iterators)) {
			// create/find the node providing the iteration number
			processIterator(iterators.get(0), graph, dataIns, distributionNode, parallelFor.getName());

			// create the output of the distribution function
			String nodeId = distributionNode.getId() + ConstantsEEModel.KeywordSeparator1
					+ ConstantsEEModel.JsonKeyConstantIterator;
			DataType dataType = DataType.Boolean;
			Task intIteratorDist = new Communication(nodeId);
			PropertyServiceData.setDataType(intIteratorDist, dataType);
			PropertyServiceDependency.addDataDependency(distributionNode, intIteratorDist,
					ConstantsEEModel.JsonKeyConstantIterator, graph);

			// make the loop body while remembering new nodes
			final Set<Task> functionsBeforeAdding = AfclCompounds.getFunctionNodes(graph);
			processTheLoopBody(parallelFor, graph, workflow);
			Set<Task> functionsAfterAdding = AfclCompounds.getFunctionNodes(graph);
			functionsAfterAdding.removeAll(functionsBeforeAdding);
			// connect all functions to the loop output
			for (Task bodyFunction : functionsAfterAdding) {
				PropertyServiceDependency.addDataDependency(intIteratorDist, bodyFunction,
						ConstantsEEModel.JsonKeyConstantIterator, graph);
			}
		} else {
			for (String iterator : iterators) {
				processIterator(iterator, graph, dataIns, distributionNode, parallelFor.getName());
			}
			// process the loop body
			processTheLoopBody(parallelFor, graph, workflow);
		}

		// process the data outs and add the aggregate function
		Optional<List<DataOuts>> dataOuts = Optional.ofNullable(parallelFor.getDataOuts());
		if (dataOuts.isPresent()) {
			for (DataOuts dataOut : dataOuts.get()) {
				attachAggregatedDataOut(dataOut, graph, parallelFor.getName());
			}
		}
	}

	/**
	 * Processes the loop body and adds all nodes.
	 * 
	 * @param parallelFor the parallelFor compound
	 * @param graph       the enactment graph
	 * @param workflow    the workflow
	 */
	protected static void processTheLoopBody(ParallelFor parallelFor, EnactmentGraph graph, Workflow workflow) {
		// process the loop body
		for (Function function : parallelFor.getLoopBody()) {
			if (function instanceof AtomicFunction) {
				AfclCompoundsAtomic.addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
			} else {
				AfclCompounds.addFunctionCompound(graph, function, workflow);
			}
		}
	}

	/**
	 * Creates the nodes modeling (a) the aggregation function of the data out and
	 * (b) the aggregated data. Attaches both nodes to the graph
	 * 
	 * @param dataOut         the processed data out
	 * @param graph           the enactment graph
	 * @param parallelForName the name of the parallelFor function
	 */
	protected static void attachAggregatedDataOut(DataOuts dataOut, EnactmentGraph graph, String parallelForName) {
		// create the aggregation function
		String aggregationId = parallelForName + ConstantsEEModel.KeywordSeparator1
				+ ConstantsEEModel.FuncNameUtilityAggregation + ConstantsEEModel.KeywordSeparator1 + dataOut.getName();
		Task aggregationNode = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask(aggregationId,
				OperationType.Aggregation, parallelForName);
		// find the source and connect the aggregation node to it
		String srcString = dataOut.getSource();
		// TODO this has to be put into a method somewhere
		Task dataToAggregate = Optional.ofNullable(graph.getVertex(srcString))
				.orElseThrow(() -> new IllegalStateException("Cannot find data to aggregate: " + srcString));
		String jsonKey = ConstantsEEModel.JsonKeyAggregation;
		PropertyServiceDependency.addDataDependency(dataToAggregate, aggregationNode, jsonKey, graph);
		// create the node for the aggregated data
		String aggregatedId = parallelForName + ConstantsAfcl.SourceAffix + dataOut.getName();
		DataType dataType = UtilsAfcl.getDataTypeForString(dataOut.getType());
		if (!dataType.equals(DataType.Collection)) {
			throw new IllegalStateException("The data out of a parallel for must be a collection.");
		}
		Task aggregatedData = new Communication(aggregatedId);
		PropertyServiceData.setDataType(aggregatedData, dataType);
		// connect it to the aggregation function
		PropertyServiceDependency.addDataDependency(aggregationNode, aggregatedData,
				ConstantsEEModel.JsonKeyAggregation, graph);
	}

	/**
	 * Returns true iff the given list specifies a number-driven execution.
	 * 
	 * @param iterators the iterator list
	 * @return true iff the given list specifies a number-driven execution
	 */
	protected static boolean isIntIteratorList(List<String> iterators) {
		if (iterators.size() == 1) {
			return isIntIterator(iterators.get(0));
		} else if (iterators.size() > 1) {
			if (iterators.stream().anyMatch(iterator -> isIntIterator(iterator))) {
				throw new IllegalArgumentException("Int iterators are only allowed in a list with a single entry.");
			}
			return false;
		} else {
			throw new IllegalArgumentException("Empty Iterator list provided.");
		}
	}

	/**
	 * Returns true iff the given iterator defines the iteration number rather than
	 * a collection to iterate over.
	 * 
	 * @param iterator        the given iterator
	 * @param parallelForName the name of the parallel for
	 * @return true iff the given iterator defines the iteration number rather than
	 *         a collection to iterate over
	 */
	protected static boolean isIntIterator(String iterator) {
		return UtilsIO.readableAsInt(iterator) || UtilsAfcl.isSrcString(iterator);
	}

	/**
	 * Processes the given iterator string by adding the necessary nodes to the
	 * graph.
	 * 
	 * @param iterator         the iterator string
	 * @param graph            the enactment graph
	 * @param dataIns          the list of data ins
	 * @param distributionNode the node modeling the distribution operation.
	 */
	protected static void processIterator(String iterator, EnactmentGraph graph, List<DataIns> dataIns,
			Task distributionNode, String parallelForName) {
		// connect the data to distribute
		if (UtilsAfcl.isSrcString(iterator)) {
			// iterator from source
			final Task inputData = AfclCompounds.assureDataNodePresence(iterator, DataType.Number, graph);
			PropertyServiceDependency.addDataDependency(inputData, distributionNode,
					ConstantsEEModel.JsonKeyConstantIterator, graph);
		} else if (UtilsIO.readableAsInt(iterator)) {
			// iterator from constant number
			int content = UtilsIO.readAsInt(iterator);
			String jsonKey = ConstantsEEModel.JsonKeyConstantIterator;
			Task dataTask = PropertyServiceData.createConstantNode(
					distributionNode.getId() + ConstantsEEModel.KeywordSeparator1 + jsonKey, DataType.Number,
					new JsonPrimitive(content));
			PropertyServiceDependency.addDataDependency(dataTask, distributionNode, jsonKey, graph);
		} else {
			// iterator from a parallelFor dataIn
			if (!dataIns.stream().anyMatch(dataIn -> dataIn.getName().equals(iterator))) {
				throw new IllegalStateException("No dataIns for the iterator " + iterator);
			}
			DataIns dataInIterator = dataIns.stream().filter(dataIn -> dataIn.getName().equals(iterator)).findAny()
					.get();
			AfclCompounds.addDataInDefault(graph, distributionNode, dataInIterator, DataType.Collection);
			String distributedDataId = parallelForName + ConstantsAfcl.SourceAffix + dataInIterator.getName();
			DataType dataType = UtilsAfcl.getDataTypeForString(dataInIterator.getType());
			Task distributedData = AfclCompounds.assureDataNodePresence(distributedDataId, dataType, graph);
			String jsonKey = ConstantsEEModel.JsonKeyDistribution;
			PropertyServiceDependency.addDataDependency(distributionNode, distributedData, jsonKey, graph);
		}
	}
}
