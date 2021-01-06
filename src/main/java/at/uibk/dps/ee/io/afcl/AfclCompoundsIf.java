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
import at.uibk.dps.afcl.functions.objects.ACondition;
import at.uibk.dps.afcl.functions.objects.DataOuts;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.objects.Condition;
import at.uibk.dps.ee.model.objects.Condition.Operator;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionSyntax;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCondition;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionSyntax.SyntaxType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility.UtilityType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCondition.Summary;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * Static method container for the methods used when creating the enactment
 * graph parts modeling elements of if compounds.
 * 
 * @author Fedor Smirnov
 */
public final class AfclCompoundsIf {

	/**
	 * No constructor.
	 */
	private AfclCompoundsIf() {
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
		Set<Task> tasksBeforeAdding = AfclCompounds.getFunctionNodes(graph);
		// add the contents of the branch
		final List<Function> functionsToAdd = isThen ? ifCompound.getThen() : ifCompound.getElse();
		for (final Function function : functionsToAdd) {
			if (function instanceof AtomicFunction) {
				AfclCompoundsAtomic.addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
			} else {
				AfclCompounds.addFunctionCompound(graph, function, workflow);
			}
		}
		// figure out which ones are new
		final Set<Task> tasksAfterAdding = AfclCompounds.getFunctionNodes(graph);
		tasksAfterAdding.removeAll(tasksBeforeAdding);
		// connect them to the condition variable
		for (final Task newTask : tasksAfterAdding) {
			Dependency dependency = PropertyServiceDependencyControlIf.createControlIfDependency(conditionalVariable,
					newTask, isThen);
			graph.addEdge(dependency, conditionalVariable, newTask, EdgeType.DIRECTED);
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
	protected static void addChoiceFunction(final EnactmentGraph graph, final DataOuts dataOut,
			final IfThenElse ifCompound, final Workflow workflow) {
		checkDataOutIfSrc(dataOut, graph);
		final String srcString = AfclApiWrapper.getSource(dataOut);
		final String firstSrc = UtilsAfcl.getFirstSubStringIfOut(srcString);
		final String secondSrc = UtilsAfcl.getSecondSubStringIfOut(srcString);
		final Task firstSrcNode = graph.getVertex(firstSrc);
		final Task secondSrcNode = graph.getVertex(secondSrc);
		// create the choice function node
		final String funcNodeId = firstSrc + ConstantsEEModel.EarliestArrivalFuncAffix + secondSrc;
		final Task choiceFunction = PropertyServiceFunctionSyntax.createSyntaxFunction(funcNodeId,
				SyntaxType.EarliestInput);
		// add the inputs (kind-of the same as data ins for an atomic)
		final Dependency inEdgeFirst = PropertyServiceDependency.createDataDependency(firstSrcNode, choiceFunction,
				ConstantsEEModel.EarliestArrivalJsonKey);
		graph.addEdge(inEdgeFirst, firstSrcNode, choiceFunction, EdgeType.DIRECTED);
		final Dependency inEdgeSecond = PropertyServiceDependency.createDataDependency(secondSrcNode, choiceFunction,
				ConstantsEEModel.EarliestArrivalJsonKey);
		graph.addEdge(inEdgeSecond, secondSrcNode, choiceFunction, EdgeType.DIRECTED);
		// add the output
		final String jsonKey = AfclApiWrapper.getName(dataOut);
		final String dataNodeId = srcString;
		final DataType dataType = UtilsAfcl.getDataTypeForString(dataOut.getType());
		// retrieve or create the data node
		final Task dataNodeOut = AfclCompounds.assureDataNodePresence(dataNodeId, dataType, graph);
		// create, annotate, and add the dependency to the graph
		final Dependency dependency = PropertyServiceDependency.createDataDependency(choiceFunction, dataNodeOut,
				jsonKey);
		graph.addEdge(dependency, choiceFunction, dataNodeOut, EdgeType.DIRECTED);
	}

	/**
	 * Checks that sanity of the data out of the if compound. Throws an exception if
	 * it detects any entries which don't make sense.
	 * 
	 * @param dataOut the data out to check
	 * @param graph   the hitherto created enactment graph
	 */
	protected static void checkDataOutIfSrc(final DataOuts dataOut, final EnactmentGraph graph) {
		final String srcString = AfclApiWrapper.getSource(dataOut);
		final String dataOutName = AfclApiWrapper.getName(dataOut);
		final String firstSrc = UtilsAfcl.getFirstSubStringIfOut(srcString);
		final String secondSrc = UtilsAfcl.getSecondSubStringIfOut(srcString);
		if (!UtilsAfcl.isIfOutSrc(srcString)) {
			throw new IllegalArgumentException("The src of data out " + AfclApiWrapper.getName(dataOut)
					+ " does not look like the out of an if compound.");
		}
		if (!UtilsAfcl.isSrcString(firstSrc)) {
			throw new IllegalArgumentException(
					"First part of the if data out " + dataOutName + " does not point to a function out.");
		}
		if (!UtilsAfcl.isSrcString(secondSrc)) {
			throw new IllegalArgumentException(
					"Second part of the if data out " + dataOutName + " does not point to a function out.");
		}
		if (graph.getVertex(firstSrc) == null) {
			throw new IllegalStateException("Src of if data out " + firstSrc + " not in the graph");
		}
		if (graph.getVertex(secondSrc) == null) {
			throw new IllegalStateException("Src of if data out " + secondSrc + " not in the graph");
		}
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
		final String nodeId = AfclApiWrapper.getName(ifCompound);
		final Task funcNode = new Task(nodeId);
		PropertyServiceFunction.setType(FunctionType.Utility, funcNode);
		PropertyServiceFunctionUtility.setUtilityType(funcNode, UtilityType.Condition);
		final Set<Condition> conditions = new HashSet<>();
		for (final ACondition afclCondition : ifCompound.getCondition().getConditions()) {
			conditions.add(addConditionNode(graph, afclCondition, funcNode, workflow));
		}
		PropertyServiceFunctionUtilityCondition.setConditions(funcNode, conditions);
		final Summary summary = UtilsAfcl.getSummaryForString(ifCompound.getCondition().getCombinedWith());
		PropertyServiceFunctionUtilityCondition.setSummary(funcNode, summary);
		final String decVarId = AfclApiWrapper.getName(ifCompound) + ConstantsEEModel.DecisionVariableSuffix;
		final Task decisionVariableNode = new Communication(decVarId);
		final Dependency dependency = PropertyServiceDependency.createDataDependency(funcNode, decisionVariableNode,
				ConstantsEEModel.DecisionVariableJsonKey);
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
		final String firstInput = getConditionDataSrc(condition.getData1(), conditionFunction.getId(), workflow);
		final String secondInput = getConditionDataSrc(condition.getData2(), conditionFunction.getId(), workflow);
		final Operator operator = UtilsAfcl.getOperatorForString(condition.getOperator());
		final DataType dataType = operator.getDataType();
		addConditionIn(graph, conditionFunction, firstInput, dataType);
		addConditionIn(graph, conditionFunction, secondInput, dataType);
		final boolean negation = AfclApiWrapper.getNegation(condition);
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
	protected static String getConditionDataSrc(final String conditionDataString, final String conditionFunctionId,
			final Workflow workflow) {
		if (UtilsAfcl.isSrcString(conditionDataString)) {
			return HierarchyLevellingAfcl.getSrcDataId(conditionDataString, workflow);
		} else {
			return conditionFunctionId + ConstantsAfcl.SourceAffix + conditionDataString;
		}
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
	protected static void addConditionIn(final EnactmentGraph graph, final Task conditionFunction,
			final String dataString, final DataType dataType) {
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
			final String dataString, final DataType dataType) {
		final String dataNodeId = conditionFunction.getId() + ConstantsAfcl.SourceAffix + dataString;
		final String jsonKey = dataNodeId;
		final JsonElement content = JsonParser.parseString(dataString);

		final Task constantDataNode = PropertyServiceData.createConstantNode(dataNodeId, dataType, content);
		final Dependency dependency = PropertyServiceDependency.createDataDependency(constantDataNode,
				conditionFunction, jsonKey);
		graph.addEdge(dependency, constantDataNode, conditionFunction, EdgeType.DIRECTED);
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
			final String dataString, final DataType dataType) {
		final String jsonKey = dataString;
		final Task dataNode = AfclCompounds.assureDataNodePresence(dataString, dataType, graph);
		final Dependency dependency = PropertyServiceDependency.createDataDependency(dataNode, conditionFunction,
				jsonKey);
		graph.addEdge(dependency, dataNode, conditionFunction, EdgeType.DIRECTED);
	}
}
