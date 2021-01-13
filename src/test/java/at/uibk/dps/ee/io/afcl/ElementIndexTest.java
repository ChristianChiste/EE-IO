package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.objects.SubCollection;
import at.uibk.dps.ee.model.objects.SubCollectionElement;
import at.uibk.dps.ee.model.objects.SubCollectionStartEndStride;
import at.uibk.dps.ee.model.objects.SubCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility.UtilityType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityElementIndex;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

public class ElementIndexTest {

	@Test
	public void testGenerateAtomicWithElementIndex() {

		Workflow atomicElementIndexWf = Graphs.getElementIndexWf();
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(atomicElementIndexWf);
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
		assertEquals(4, dataNum);
		assertEquals(2, functionNum);

		// that we have the right predecessor relations and the right naming
		Task functionNode = result.getVertex(ConstantsTestCoreEEiO.wfFunctionNameAtomic);
		Task dataNodeProcessed = result.getPredecessors(functionNode).iterator().next();
		Task dataProcessingNode = result.getPredecessors(dataNodeProcessed).iterator().next();
		Task dataRawNode = result.getPredecessors(dataProcessingNode).iterator().next();

		assertEquals(ConstantsTestCoreEEiO.elementIndexRawDataName, dataRawNode.getId());
		assertEquals(ConstantsTestCoreEEiO.elementIndexDataProcessingName, dataProcessingNode.getId());
		assertEquals(ConstantsTestCoreEEiO.elementIndexProcessedDataName, dataNodeProcessed.getId());

		// that the function node is annotated correctly
		assertEquals(UtilityType.ElementIndex, PropertyServiceFunctionUtility.getUtilityType(dataProcessingNode));
		SubCollections subCollections = PropertyServiceFunctionUtilityElementIndex
				.getSubCollections(dataProcessingNode);
		assertEquals(2, subCollections.size());
		SubCollection first = subCollections.get(0);
		SubCollection second = subCollections.get(1);

		assertTrue(first instanceof SubCollectionElement);
		assertTrue(second instanceof SubCollectionStartEndStride);

		assertEquals("0", first.toString());
		assertEquals("3:6:2", second.toString());
	}
}
