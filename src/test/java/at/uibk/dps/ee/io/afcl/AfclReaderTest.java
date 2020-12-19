package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;
import at.uibk.dps.afcl.Workflow;

public class AfclReaderTest {

	
	@Test
	public void testBytesToWf() {
		Workflow wf = Graphs.getSingleAtomicWf();
		// generic poking around to check that we actually get a wf. The functionality
		// itself is tested in AFCLCore
		assertEquals("single Atomic", wf.getName());
		assertEquals(1, wf.getWorkflowBody().size());
	}
	

}
