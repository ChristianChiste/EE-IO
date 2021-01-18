package at.uibk.dps.ee.io.afcl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.PropertyConstraint;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCollections.CollectionOperation;
import net.sf.opendse.model.Task;

/**
 * Static method container with methods used to model the operations
 * transforming collections.
 * 
 * @author Fedor Smirnov
 */
public final class AfclCollectionOperations {

	private AfclCollectionOperations() {
	}

	/**
	 * Processes the dataIn's list of collections constraints and adjusts the graph
	 * accordingly. Returns the data node with the processed data.
	 * 
	 * @param dataIn        the given dataIn
	 * @param originalData  the node modeling the collection data before the
	 *                      processing
	 * @param graph         the graph
	 * @param finalDataType the data type ultimately expected by the consumer
	 *                      function
	 * @return the data node with the processed data
	 */
	public static Task modelCollectionOperations(DataIns dataIn, Task originalData, EnactmentGraph graph,
			DataType finalDataType) {
		if (!PropertyServiceData.getDataType(originalData).equals(DataType.Collection)) {
			throw new IllegalStateException(
					"Collections operations applied to non-collection data " + originalData.getId());
		}
		// get a list of the collection constraints
		List<PropertyConstraint> collectionConstraints = dataIn.getConstraints().stream()
				.filter(constraint -> isCollectionConstraint(constraint)).collect(Collectors.toList());
		Task processedData = originalData;
		for (PropertyConstraint contraint : collectionConstraints) {
			processedData = modelCollectionOperation(contraint, dataIn.getName(), processedData, graph, finalDataType);
		}
		return processedData;
	}

	/**
	 * Same as the above method, but w.r.t. one collection operation. Applied
	 * iteratively for operation chaining.
	 * 
	 * @param contraint    the processed constraint
	 * @param jsonKey      the key that the consumer will use to access the
	 *                     processed data
	 * @param originalData data before processing with this constraint
	 * @param graph        the enactment graph
	 * @return the node with the processed data (from the one operation)
	 */
	protected static Task modelCollectionOperation(PropertyConstraint contraint, String jsonKey, Task originalData,
			EnactmentGraph graph, DataType finalDataType) {
		// create the function node modeling the operation
		String subCollectionString = contraint.getValue();
		CollectionOperation operationType = UtilsAfcl.getCollectionOperationType(contraint.getName(),
				subCollectionString);
		Task operationNode = PropertyServiceFunctionUtilityCollections.createCollectionOperation(originalData.getId(),
				subCollectionString, operationType);
		// connect it to the original data
		PropertyServiceDependency.addDataDependency(originalData, operationNode, jsonKey, graph);
		// make the src connections where necessary
		List<String> inputStrings = getSubstrings(subCollectionString, operationType);
		boolean illegalStringsPresent = !inputStrings.stream()
				.allMatch(string -> isLegalEntryString(string, operationType));
		if (illegalStringsPresent) {
			throw new IllegalArgumentException("Illegal collection operation strings: " + inputStrings);
		}
		Set<String> srcInputStrings = inputStrings.stream().filter(string -> UtilsAfcl.isSrcString(string))
				.collect(Collectors.toSet());
		srcInputStrings.forEach(srcString -> attachOperationInput(srcString, operationNode, graph));

		// create the node for the processed data
		String processedDataId = originalData.getId() + ConstantsEEModel.KeyWordSeparator2 + operationType.name()
				+ ConstantsEEModel.KeyWordSeparator2 + subCollectionString;
		boolean oneElementResult = operationType.equals(CollectionOperation.ElementIndex) && inputStrings.size() == 1;
		DataType processedDataType = oneElementResult ? finalDataType : DataType.Collection;
		Task processedData = AfclCompounds.assureDataNodePresence(processedDataId, processedDataType, graph);
		PropertyServiceDependency.addDataDependency(operationNode, processedData, jsonKey, graph);
		return processedData;
	}

	/**
	 * Connects the collection operation node to a data node supplying one of its
	 * inputs.
	 * 
	 * @param srcString     string denoting the data src (and used as json key of
	 *                      the dependency)
	 * @param operationNode the function node
	 * @param graph         the enactment graph
	 */
	protected static void attachOperationInput(String srcString, Task operationNode, EnactmentGraph graph) {
		Task inputData = AfclCompounds.assureDataNodePresence(srcString, DataType.Number, graph);
		PropertyServiceDependency.addDataDependency(inputData, operationNode, srcString, graph);
	}

	/**
	 * Converts the subcollection string into a list of strings, where each entry
	 * could be a source string
	 * 
	 * @param subcollectionString
	 * @param operation
	 * @return
	 */
	protected static List<String> getSubstrings(String subcollectionString, CollectionOperation operation) {
		List<String> result = new ArrayList<>();
		if (operation.equals(CollectionOperation.Replicate) || operation.equals(CollectionOperation.Split)) {
			result.add(subcollectionString);
			return result;
		} else if (operation.equals(CollectionOperation.Block)) {
			if (!subcollectionString.contains(ConstantsAfcl.constraintSeparatorBlock)) {
				throw new IllegalArgumentException("Incorrect Block Constraint Argument: " + subcollectionString);
			}
			result.add(subcollectionString.split(ConstantsAfcl.constraintSeparatorBlock)[0]);
			result.add(subcollectionString.split(ConstantsAfcl.constraintSeparatorBlock)[1]);
			return result;
		} else if (operation.equals(CollectionOperation.ElementIndex)) {
			if (subcollectionString.contains(ConstantsAfcl.constraintSeparatorEIdxOuter)) {
				for (String innerSubString : subcollectionString.split(ConstantsAfcl.constraintSeparatorEIdxOuter)) {
					result.addAll(getInnerEidxSubstrings(innerSubString));
				}
			} else {
				result.addAll(getInnerEidxSubstrings(subcollectionString));
			}
			return result;
		} else {
			throw new IllegalStateException("Unknown collection operation: " + operation.name());
		}
	}

	/**
	 * Splits the given string describing one Eidx access into a list of substrings,
	 * each of which is a number, emtpy, or a src string.
	 * 
	 * @param innerEidxString
	 * @return
	 */
	protected static List<String> getInnerEidxSubstrings(String innerEidxString) {
		List<String> result = new ArrayList<>();
		if (innerEidxString.contains(ConstantsAfcl.constraintSeparatorEIdxInner)) {
			result.addAll(Arrays.asList(innerEidxString.split(ConstantsAfcl.constraintSeparatorEIdxInner)));
			return result;
		} else {
			result.add(innerEidxString);
			return result;
		}
	}

	/**
	 * Returns true if the given substring is legal.
	 * 
	 * @param entryString the string to check
	 * @param operation   the operation type
	 * @return true if the given substring is legal.
	 */
	protected static boolean isLegalEntryString(String entryString, CollectionOperation operation) {
		if (UtilsAfcl.isSrcString(entryString)) {
			return true;
		}
		String noWsString = entryString.trim();
		if (operation.equals(CollectionOperation.ElementIndex) && noWsString.isEmpty()) {
			return true;
		}
		if (UtilsAfcl.isInt(noWsString)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the provided dataIn has at least one constraint defining a
	 * collection operation.
	 * 
	 * @param dataIn the given dataIn
	 * @return true if the provided dataIn has at least one constraint defining a
	 *         collection operation
	 */
	public static boolean hasCollectionOperations(DataIns dataIn) {
		if (!AfclApiWrapper.hasConstraints(dataIn)) {
			return false;
		}
		return dataIn.getConstraints().stream().anyMatch(constraint -> isCollectionConstraint(constraint));
	}

	/**
	 * Returns true if the provided constraint relates to collection operations.
	 * 
	 * @param constraint the provided constraint.
	 * @return true if the provided constraint relates to collection operations
	 */
	protected static boolean isCollectionConstraint(PropertyConstraint constraint) {
		final String name = constraint.getName();
		return name.equals(ConstantsAfcl.constraintNameBlock) || name.equals(ConstantsAfcl.constraintNameElementIndex)
				|| name.equals(ConstantsAfcl.constraintNameReplicate);
	}
}