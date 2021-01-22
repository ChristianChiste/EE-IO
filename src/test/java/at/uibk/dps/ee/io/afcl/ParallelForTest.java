package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow.DataFlowType;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

public class ParallelForTest {

	@Test
	public void testParFor() {
		Workflow wf = Graphs.getParallelForWf();
		// get the enactment graph
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(wf);
		EnactmentGraphViewer.view(result);

		// check the func and the data count
		long funcCount = result.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task)).count();
		long dataCount = result.getVertices().stream().filter(task -> TaskPropertyService.isCommunication(task))
				.count();

		assertEquals(5, funcCount);
		assertEquals(7, dataCount);

		// check root and leaf number
		assertEquals(2, result.getVertices().stream().filter(task -> TaskPropertyService.isCommunication(task) && PropertyServiceData.isRoot(task)).count());
		assertEquals(1, result.getVertices().stream().filter(task -> TaskPropertyService.isCommunication(task) && PropertyServiceData.isLeaf(task)).count());

		// get all the nodes
		Task output = result.getVertices().stream().filter(task -> TaskPropertyService.isCommunication(task) && PropertyServiceData.isLeaf(task)).findAny()
				.orElseThrow(() -> new AssertionError());
		assertEquals(DataType.Collection, PropertyServiceData.getDataType(output));
		Task aggregationNode = result.getPredecessors(output).iterator().next();
		Task aggregationData = result.getPredecessors(aggregationNode).iterator().next();
		assertEquals(DataType.String, PropertyServiceData.getDataType(aggregationData));
		Task function2 = result.getPredecessors(aggregationData).iterator().next();
		Task intermediateData = result.getPredecessors(function2).iterator().next();
		assertEquals(DataType.Number, PropertyServiceData.getDataType(intermediateData));

		Task distInput = result.getVertex(ConstantsTestCoreEEiO.parForRawDistDataName);
		Task nonDistributedCollection = result.getVertex(ConstantsTestCoreEEiO.parForRawConstDataName);
		assertEquals(DataType.Collection, PropertyServiceData.getDataType(nonDistributedCollection));

		Task blockFunc = result.getSuccessors(distInput).iterator().next();
		Task blockData = result.getSuccessors(blockFunc).iterator().next();
		assertEquals(DataType.Collection, PropertyServiceData.getDataType(blockData));
		Task distributionNode = result.getSuccessors(blockData).iterator().next();
		Task distributionData = result.getSuccessors(distributionNode).iterator().next();
		assertEquals(DataType.Number, PropertyServiceData.getDataType(distributionData));

		// check the annotation

		// dist and aggr nodes
		assertEquals(DataFlowType.Aggregation, PropertyServiceFunctionDataFlow.getDataFlowType(aggregationNode));
		assertEquals(DataFlowType.Distribution, PropertyServiceFunctionDataFlow.getDataFlowType(distributionNode));

		// check the edge json key annotation

	}

	public void testParForMoreComplex() {
		Workflow wf = Graphs.getParallelForWf();
		// get the enactment graph
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(wf);

		// check the func and the data count
		long funcCount = result.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task)).count();
		long dataCount = result.getVertices().stream().filter(task -> TaskPropertyService.isCommunication(task))
				.count();

		assertEquals(5, funcCount);
		assertEquals(11, dataCount);

		// get the distribution and the aggregation function
		Task distributionNode = Optional.ofNullable(result.getVertex(ConstantsTestCoreEEiO.parForRawDistDataName))
				.orElseThrow(() -> new AssertionError("Dist not found"));
		Task aggregationNode = Optional.ofNullable(result.getVertex(ConstantsTestCoreEEiO.parForRawAggrNodeName))
				.orElseThrow(() -> new AssertionError("Aggr not found"));
		
		// check the predecessor and successor counts
		assertEquals(4, result.getPredecessorCount(distributionNode));
		assertEquals(2, result.getSuccessorCount(distributionNode));
		
		assertEquals(2, result.getPredecessorCount(aggregationNode));
		assertEquals(2, result.getSuccessorCount(aggregationNode));
	}
}
