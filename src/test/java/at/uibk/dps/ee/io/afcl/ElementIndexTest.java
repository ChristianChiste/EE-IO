package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility.UtilityType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityElementIndex;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

public class ElementIndexTest {

	@Test
	public void testGenerateAtomicWithElementIndex() {

		Workflow atomicElementIndexWf = Graphs.getElementIndexWf();
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(atomicElementIndexWf);
		EnactmentGraphViewer.view(result);
		// we can assume that the atomic part is done right

		// what we want to check is that
		// we have one more data and function node
		int functionNum = 0;
		int dataNum = 0;
		for (Task task : result) {
			if (TaskPropertyService.isProcess(task)) {
				functionNum++;
			} else {
				dataNum++;
			}
		}
		assertEquals(6, dataNum);
		assertEquals(2, functionNum);

		// that we have the right predecessor relations and the right naming
		Task functionNode = result.getVertex(ConstantsTestCoreEEiO.wfFunctionNameAtomic);
		Task dataNodeProcessed = result.getPredecessors(functionNode).iterator().next();
		Task dataProcessingNode = result.getPredecessors(dataNodeProcessed).iterator().next();

		assertEquals(3, result.getPredecessorCount(dataProcessingNode));
		Task dataRawNode = result.getVertex(ConstantsTestCoreEEiO.elementIndexRawDataName);
		Task dataInputStride = result.getVertex(ConstantsTestCoreEEiO.elementIndexInputStride);
		Task dataInputIndex = result.getVertex(ConstantsTestCoreEEiO.elementIndexInputIndex);
		assertTrue(result.getPredecessors(dataProcessingNode).contains(dataRawNode));
		assertTrue(result.getPredecessors(dataProcessingNode).contains(dataInputIndex));
		assertTrue(result.getPredecessors(dataProcessingNode).contains(dataInputStride));

		assertEquals(ConstantsTestCoreEEiO.elementIndexRawDataName, dataRawNode.getId());
		assertEquals(ConstantsTestCoreEEiO.elementIndexDataProcessingName, dataProcessingNode.getId());
		assertEquals(ConstantsTestCoreEEiO.elementIndexProcessedDataName, dataNodeProcessed.getId());

		// that the function node is annotated correctly
		assertEquals(UtilityType.ElementIndex, PropertyServiceFunctionUtility.getUtilityType(dataProcessingNode));
		assertEquals(ConstantsTestCoreEEiO.elementExpectedSubCollString,
				PropertyServiceFunctionUtilityElementIndex.getSubCollectionsString(dataProcessingNode));
		// check the json keys of the src inputs
		Dependency strideOut = result.getOutEdges(dataInputStride).iterator().next();
		Dependency indexOut = result.getOutEdges(dataInputIndex).iterator().next();

		String expectedDepStride = ConstantsEEModel.EIdxParameters.Stride.name() + ConstantsEEModel.EIdxEdgeIdxSeparator + 1;
		String expectedDepIndex = ConstantsEEModel.EIdxParameters.Index.name() + ConstantsEEModel.EIdxEdgeIdxSeparator + 0;

		assertEquals(expectedDepIndex, PropertyServiceDependency.getJsonKey(indexOut));
		assertEquals(expectedDepStride, PropertyServiceDependency.getJsonKey(strideOut));
	}
}
