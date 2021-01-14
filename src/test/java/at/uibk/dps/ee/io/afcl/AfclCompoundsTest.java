package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityElementIndex;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceData.NodeType;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

public class AfclCompoundsTest {

	@Test
	public void testProcessEIdxString() {
		String eidxString = "0, 1:2:function/stride";
		EnactmentGraph graph = new EnactmentGraph();
		Task result = AfclCompounds.processEIdxAfclString(eidxString, "dataId", graph);
		String collString = PropertyServiceFunctionUtilityElementIndex.getSubCollectionsString(result);
		assertEquals("0,1:2:data", collString);
		assertEquals(1, graph.getPredecessorCount(result));
	}
	
	@Test
	public void testAddConstantDataNode() {
		String funcName = "func";
		EnactmentGraph graph = new EnactmentGraph();
		Task function = new Task(funcName);
		graph.addVertex(function);

		String dataInName = "secondIn";
		DataIns dataIn = new DataIns();
		dataIn.setName(dataInName);
		dataIn.setType("number");
		dataIn.setSource("5");

		AfclCompounds.addDataInConstant(graph, function, dataIn);

		assertEquals(1, graph.getEdgeCount());
		assertEquals(2, graph.getVertexCount());

		Dependency dep = graph.getEdges().iterator().next();
		Task data = graph.getSource(dep);
		Task func = graph.getDest(dep);

		assertEquals(func, function);

		assertEquals(dataInName, PropertyServiceDependency.getJsonKey(dep));
		assertEquals(funcName + ConstantsAfcl.SourceAffix + dataInName, data.getId());
		assertEquals(NodeType.Constant, PropertyServiceData.getNodeType(data));
		assertEquals(DataType.Number, PropertyServiceData.getDataType(data));
		assertEquals(5, PropertyServiceData.getContent(data).getAsInt());
	}

}
