package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionServerless;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

public class GraphGenerationAfclTest {

	@Test
	public void testGenerateSingleAtomicGraph() {

		Workflow singleAtomicWf = Graphs.getSingleAtomicWf();
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(singleAtomicWf);

		// test the general edge and node numbers
		assertEquals(3, result.getVertexCount());
		assertEquals(2, result.getEdgeCount());

		// test that we have the data ins and the data outs, set as root and leaf, type
		// as well

		// get the wf data outs and ins
		Set<Task> roots = new HashSet<>();
		Set<Task> leaves = new HashSet<>();
		Set<Task> processes = new HashSet<>();

		for (Task t : result) {
			if (result.getPredecessorCount(t) == 0) {
				roots.add(t);
			}
			if (result.getSuccessorCount(t) == 0) {
				leaves.add(t);
			}
			if (TaskPropertyService.isProcess(t)) {
				processes.add(t);
			}
		}

		assertEquals(1, roots.size());
		assertEquals(1, leaves.size());
		assertEquals(1, processes.size());

		// check the nodes

		// check the root
		Task root = roots.iterator().next();
		assertTrue(PropertyServiceData.isRoot(root));
		assertEquals(UtilsAfcl.getDataNodeId(ConstantsTestCoreEEiO.wfNameAtomic, ConstantsTestCoreEEiO.inputNameAtomic),
				root.getId());
		assertEquals(ConstantsTestCoreEEiO.wfInputJsonNameAtomic, PropertyServiceData.getJsonKey(root));
		assertEquals(DataType.Number, PropertyServiceData.getDataType(root));

		// check the leaf
		Task leaf = leaves.iterator().next();
		assertTrue(PropertyServiceData.isLeaf(leaf));
		assertEquals(UtilsAfcl.getDataNodeId(ConstantsTestCoreEEiO.wfFunctionNameAtomic,
				ConstantsTestCoreEEiO.wfFunctionOutputNameAtomic), leaf.getId());
		assertEquals(ConstantsTestCoreEEiO.outputNameAtomic, PropertyServiceData.getJsonKey(leaf));
		assertEquals(DataType.String, PropertyServiceData.getDataType(leaf));

		// check the function
		Task func = processes.iterator().next();
		assertEquals(ConstantsTestCoreEEiO.wfFunctionNameAtomic, func.getId());
		assertEquals(FunctionType.Serverless, PropertyServiceFunction.getType(func));
		assertEquals(ConstantsTestCoreEEiO.wfFunctionResourceNameAtomic,
				PropertyServiceFunctionServerless.getResource(func));

		// Check the edges
		Set<Dependency> inEdges = new HashSet<Dependency>(result.getInEdges(func));
		Set<Dependency> outEdges = new HashSet<Dependency>(result.getOutEdges(func));

		assertEquals(1, inEdges.size());
		assertEquals(1, outEdges.size());

		Dependency inputEdge = inEdges.iterator().next();
		Dependency outputEdge = outEdges.iterator().next();

		assertEquals(root, result.getEndpoints(inputEdge).getFirst());
		assertEquals(leaf, result.getEndpoints(outputEdge).getSecond());

		assertEquals(ConstantsTestCoreEEiO.wfFunctionInputNameAtomic, PropertyServiceDependency.getJsonKey(inputEdge));
		assertEquals(ConstantsTestCoreEEiO.wfFunctionOutputNameAtomic,
				PropertyServiceDependency.getJsonKey(outputEdge));
	}
}
