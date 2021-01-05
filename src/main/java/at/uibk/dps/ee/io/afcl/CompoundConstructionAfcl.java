package at.uibk.dps.ee.io.afcl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.IfThenElse;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.afcl.functions.objects.ACondition;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOuts;
import at.uibk.dps.afcl.functions.objects.DataOutsAtomic;
import at.uibk.dps.afcl.functions.objects.Section;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.objects.Condition;
import at.uibk.dps.ee.model.objects.Condition.Operator;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionSyntax.SyntaxType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility.UtilityType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCondition;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCondition.Summary;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionServerless;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionSyntax;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility;
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
		case If: {
			addIf(graph, (IfThenElse) function, workflow);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + UtilsAfcl.getCompoundType(function));
		}
	}

	/**
	 * Adds the nodes and edges modeling the content of the given if compound.
	 * 
	 * @param graph      the enactment graph
	 * @param ifCompound the if compound to add
	 * @param workflow   the afcl workflow object
	 */
	protected static void addIf(final EnactmentGraph graph, final IfThenElse ifCompound, final Workflow workflow) {
		// create and add the condition function, get the condition variable
		Task conditionVariable = addConditionFunction(graph, ifCompound, workflow);
		// add the then branch
		addIfBranch(graph, ifCompound, workflow, conditionVariable, true);
		// add the else branch
		addIfBranch(graph, ifCompound, workflow, conditionVariable, false);
		// create and add a choice function for each data out
		for (DataOuts dataOut : AfclApiWrapper.getDataOuts(ifCompound)) {
			addChoiceFunction(graph, dataOut, ifCompound, workflow);
		}
	}

	/**
	 * Adds the choice function and the data modeling the result of the if compound
	 * described by the given data out.
	 * 
	 * @param graph      the enactment graph
	 * @param dataOut    the given data out
	 * @param ifCompound the given if compound
	 * @param workflow   the afcl workflow
	 */
	protected static void addChoiceFunction(EnactmentGraph graph, DataOuts dataOut, IfThenElse ifCompound,
			Workflow workflow) {
		String srcString = AfclApiWrapper.getSource(dataOut);
		if (!UtilsAfcl.isIfOutSrc(srcString)) {
			throw new IllegalArgumentException("The src of data out " + AfclApiWrapper.getName(dataOut)
					+ " does not look like the out of an if compound.");
		}
		String firstSrc = UtilsAfcl.getFirstSubStringIfOut(srcString);
		String secondSrc = UtilsAfcl.getSecondSubStringIfOut(srcString);
		if (!UtilsAfcl.isSrcString(firstSrc)) {
			throw new IllegalArgumentException("First part of the if data out does not point to a function out.");
		}
		if (!UtilsAfcl.isSrcString(secondSrc)) {
			throw new IllegalArgumentException("Second part of the if data out does not point to a function out.");
		}

		Task firstSrcNode = graph.getVertex(firstSrc);
		Task secondSrcNode = graph.getVertex(secondSrc);

		if (firstSrcNode == null) {
			throw new IllegalStateException("Src of if data out " + firstSrc + " not in the graph");
		}

		if (secondSrcNode == null) {
			throw new IllegalStateException("Src of if data out " + secondSrc + " not in the graph");
		}

		// create the choice function node
		String funcNodeId = firstSrc + ConstantsEEModel.EarliestArrivalFuncAffix + secondSrc;
		Task choiceFunction = PropertyServiceFunctionSyntax.createSyntaxFunction(funcNodeId, SyntaxType.EarliestInput);

		// add the inputs (kind-of the same as data ins for an atomic)
		Dependency inEdgeFirst = PropertyServiceDependency.createDependency(firstSrcNode, choiceFunction);
		PropertyServiceDependency.setType(inEdgeFirst, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(inEdgeFirst, ConstantsEEModel.EarliestArrivalJsonKey);
		graph.addEdge(inEdgeFirst, firstSrcNode, choiceFunction, EdgeType.DIRECTED);

		Dependency inEdgeSecond = PropertyServiceDependency.createDependency(secondSrcNode, choiceFunction);
		PropertyServiceDependency.setType(inEdgeSecond, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(inEdgeSecond, ConstantsEEModel.EarliestArrivalJsonKey);
		graph.addEdge(inEdgeSecond, secondSrcNode, choiceFunction, EdgeType.DIRECTED);

		// add the output
		final String jsonKey = AfclApiWrapper.getName(dataOut);
		final String dataNodeId = srcString;
		final DataType dataType = UtilsAfcl.getDataTypeForString(dataOut.getType());
		// retrieve or create the data node
		final Task dataNodeOut = assureDataNodePresence(dataNodeId, dataType, graph);
		// create, annotate, and add the dependency to the graph
		final Dependency dependency = PropertyServiceDependency.createDependency(choiceFunction, dataNodeOut);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, choiceFunction, dataNodeOut, EdgeType.DIRECTED);
	}

	/**
	 * Adds the node modeling the evaluation of the condition function of the if
	 * compound. Returns the data node modeling the condition variable.
	 * 
	 * @param graph      the enactment graph
	 * @param ifCompound the if compound that is being modeled
	 * @param workflow   the afcl workflow object
	 * @return the data node modeling the condition variable
	 */
	protected static Task addConditionFunction(final EnactmentGraph graph, final IfThenElse ifCompound,
			final Workflow workflow) {
		String nodeId = AfclApiWrapper.getName(ifCompound);
		Task funcNode = new Task(nodeId);
		PropertyServiceFunction.setType(FunctionType.Utility, funcNode);
		PropertyServiceFunctionUtility.setUtilityType(funcNode, UtilityType.Condition);
		Set<Condition> conditions = new HashSet<>();
		for (ACondition afclCondition : ifCompound.getCondition().getConditions()) {
			conditions.add(addConditionNode(graph, afclCondition, funcNode, workflow));
		}
		PropertyServiceFunctionUtilityCondition.setConditions(funcNode, conditions);
		Summary summary = UtilsAfcl.getSummaryForString(ifCompound.getCondition().getCombinedWith());
		PropertyServiceFunctionUtilityCondition.setSummary(funcNode, summary);
		String decVarId = AfclApiWrapper.getName(ifCompound) + ConstantsEEModel.DecisionVariableSuffix;
		Task decisionVariableNode = new Communication(decVarId);
		Dependency dependency = PropertyServiceDependency.createDependency(funcNode, decisionVariableNode);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, ConstantsEEModel.DecisionVariableJsonKey);
		graph.addEdge(dependency, funcNode, decisionVariableNode, EdgeType.DIRECTED);
		return decisionVariableNode;
	}

	/**
	 * Adds the data required by the given afcl condition to the enactment graph.
	 * Returns the condition object.
	 * 
	 * @param graph             the enactment graph
	 * @param condition         the afcl condition
	 * @param conditionFunction the task modeling the condition function
	 */
	protected static Condition addConditionNode(final EnactmentGraph graph, final ACondition condition,
			Task conditionFunction, Workflow workflow) {
		String firstInput = getConditionDataSrc(condition.getData1(), conditionFunction.getId(), workflow);
		String secondInput = getConditionDataSrc(condition.getData2(), conditionFunction.getId(), workflow);
		Operator operator = UtilsAfcl.getOperatorForString(condition.getOperator());
		DataType dataType = operator.getDataType();
		addConditionIn(graph, conditionFunction, firstInput, dataType);
		addConditionIn(graph, conditionFunction, secondInput, dataType);
		boolean negation = AfclApiWrapper.getNegation(condition);
		return new Condition(firstInput, secondInput, operator, negation);
	}

	/**
	 * Returns the actual src string for the given condition data.
	 * 
	 * @param conditionDataString the condition string
	 * @param conditionFunctionId the id of the function node evaluating the
	 *                            condition
	 * @param workflow            the processed workflow
	 * @return the actual src string for the given condition data
	 */
	protected static String getConditionDataSrc(String conditionDataString, String conditionFunctionId,
			Workflow workflow) {
		if (UtilsAfcl.isSrcString(conditionDataString)) {
			return HierarchyLevellingAfcl.getSrcDataId(conditionDataString, workflow);
		} else {
			return conditionFunctionId + ConstantsAfcl.SourceAffix + conditionDataString;
		}
	}

	/**
	 * Adds the nodes modeling an if branch of the given if compound
	 * 
	 * @param graph               the enactment graph
	 * @param ifCompound          the if compound that is being modeled
	 * @param workflow            the afcl workflow object
	 * @param conditionalVariable the condition variable
	 * @param isThen              true iff modeling the then branch
	 */
	protected static void addIfBranch(final EnactmentGraph graph, final IfThenElse ifCompound, final Workflow workflow,
			Task conditionalVariable, boolean isThen) {
		// remember all function nodes in the graph now
		Set<Task> tasksBeforeAdding = getFunctionNodes(graph);
		// add the contents of the branch
		List<Function> functionsToAdd = isThen ? ifCompound.getThen() : ifCompound.getElse();
		for (Function function : functionsToAdd) {
			if (function instanceof AtomicFunction) {
				addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
			} else {
				addFunctionCompound(graph, function, workflow);
			}
		}
		// figure out which ones are new
		Set<Task> tasksAfterAdding = getFunctionNodes(graph);
		tasksAfterAdding.removeAll(tasksBeforeAdding);
		// connect them to the condition variable
		for (Task newTask : tasksAfterAdding) {
			Dependency dependency = PropertyServiceDependencyControlIf.createControlIfDependency(conditionalVariable,
					newTask, isThen);
			graph.addEdge(dependency, conditionalVariable, newTask, EdgeType.DIRECTED);

		}
	}

	/**
	 * Returns a set of all function nodes in the given graph.
	 * 
	 * @param graph the given graph
	 * @return a set of all function nodes in the given graph
	 */
	protected static Set<Task> getFunctionNodes(EnactmentGraph graph) {
		Set<Task> result = new HashSet<>();
		for (Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				result.add(task);
			}
		}
		return result;
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
			if (!UtilsAfcl.isSrcString(srcString)) {
				// constant data in
				continue;
			}
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
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, constantDataNode, function, EdgeType.DIRECTED);
	}

	/**
	 * Adds the data node representing an input (either a constant or sth with a
	 * source) for a condition evaluation.
	 * 
	 * @param graph             the enactment graph
	 * @param conditionFunction the function node modeling the condition function
	 * @param dataString        the data string
	 * @param dataType          the data type
	 */
	protected static void addConditionIn(final EnactmentGraph graph, final Task conditionFunction, String dataString,
			DataType dataType) {
		if (UtilsAfcl.isSrcString(dataString)) {
			addConditionInDefault(graph, conditionFunction, dataString, dataType);
		} else {
			addConditionInConstant(graph, conditionFunction, dataString, dataType);
		}
	}

	/**
	 * Adds the dependency (and the data node) to model the constant data required
	 * for the evaluation of the condition function.
	 * 
	 * @param graph             the enactment graph
	 * @param conditionFunction the node modeling the condition function
	 * @param dataString        the src string
	 * @param dataType          the expected data type
	 */
	protected static void addConditionInConstant(final EnactmentGraph graph, final Task conditionFunction,
			String dataString, DataType dataType) {
		String dataNodeId = conditionFunction.getId() + ConstantsAfcl.SourceAffix + dataString;
		String jsonKey = dataNodeId;
		JsonElement content = JsonParser.parseString(dataString);

		Task constantDataNode = PropertyServiceData.createConstantNode(dataNodeId, dataType, content);
		Dependency dependency = PropertyServiceDependency.createDependency(constantDataNode, conditionFunction);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, constantDataNode, conditionFunction, EdgeType.DIRECTED);
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
	 * Adds the dependency (and the data node) to model the data required for the
	 * evaluation of the condition function.
	 * 
	 * @param graph             the enactment graph
	 * @param conditionFunction the node modeling the condition function
	 * @param dataString        the src string
	 * @param dataType          the expected data type
	 */
	protected static void addConditionInDefault(final EnactmentGraph graph, final Task conditionFunction,
			String dataString, DataType dataType) {
		String jsonKey = dataString;
		Task dataNode = assureDataNodePresence(dataString, dataType, graph);
		Dependency dependency = PropertyServiceDependency.createDependency(dataNode, conditionFunction);
		PropertyServiceDependency.setType(dependency, TypeDependency.Data);
		PropertyServiceDependency.setJsonKey(dependency, jsonKey);
		graph.addEdge(dependency, dataNode, conditionFunction, EdgeType.DIRECTED);
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
