package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;

public class AfclApiWrapperTest {

	@Test(expected = IllegalStateException.class)
	public void testPointsToInputExc() {
		Workflow seqParWf = Graphs.getSeqParWf();
		String sourceString = "outerSeq/bla";
		String funcName = "outerSeq";
		AfclApiWrapper.pointsToInput(sourceString, AfclApiWrapper.getFunction(seqParWf, funcName));
	}
	
	@Test
	public void testPointsToInput() {
		Workflow seqParWf = Graphs.getSeqParWf();
		String sourceString = "outerSeq/oSeqIn2";
		String funcName = "outerSeq";
		assertTrue(AfclApiWrapper.pointsToInput(sourceString, AfclApiWrapper.getFunction(seqParWf, funcName)));
		
		sourceString = "outerSeq/result";
		funcName = "outerSeq";
		assertFalse(AfclApiWrapper.pointsToInput(sourceString, AfclApiWrapper.getFunction(seqParWf, funcName)));
	}
	
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
