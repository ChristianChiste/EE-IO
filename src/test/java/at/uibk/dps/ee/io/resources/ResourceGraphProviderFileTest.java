package at.uibk.dps.ee.io.resources;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceResource;
import at.uibk.dps.ee.model.properties.PropertyServiceResource.ResourceType;
import net.sf.opendse.model.Resource;

public class ResourceGraphProviderFileTest {

	@Test
	public void testGetResourceGraph() {
		/*String filePath = ConstantsTestCoreEEiO.resourceTestInputPath;
		ResourceGraphProviderFile tested = new ResourceGraphProviderFile(filePath);
		ResourceGraph result = tested.getResourceGraph();
		assertEquals(3, result.getVertexCount());
		assertEquals(2, result.getEdgeCount());
		
		assertNotNull(result.getVertex(ConstantsEEModel.idLocalResource));
		Resource ee = result.getVertex(ConstantsEEModel.idLocalResource);
		assertEquals(ResourceType.Local, PropertyServiceResource.getResourceType(ee));*/
	}
}
