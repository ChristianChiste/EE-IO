package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;

public class AfclApiWrapperTest {

	@Test
	public void testSearchForFunction() {
		Workflow seqParWf = Graphs.getSeqParWf();
		assertNotNull(AfclApiWrapper.getFunction(seqParWf, "func5"));
		assertNotNull(AfclApiWrapper.getFunction(seqParWf, "parallel2"));
	}

	@Test(expected = IllegalStateException.class)
	public void testSearchForFunctionNotThere() {
		Workflow seqParWf = Graphs.getSeqParWf();
		AfclApiWrapper.getFunction(seqParWf, "bla");
	}
	
}
