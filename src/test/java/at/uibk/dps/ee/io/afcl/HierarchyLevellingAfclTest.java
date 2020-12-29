package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;

public class HierarchyLevellingAfclTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNoAtomicOutExc() {
		Workflow seqParWf = Graphs.getSeqParWf();

		// pointing to compound in
		String srcString = "func5/output";
		HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf);
	}

	@Test
	public void testgetSrcDataIdSeqPar() {
		Workflow seqParWf = Graphs.getSeqParWf();

		// no remapping
		// for the wf input
		String srcString = "seqParWf/input3";
		String expected = "seqParWf/input3";
		assertEquals(expected, HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf));

		// for a function output
		srcString = "func1/output";
		expected = "func1/output";
		assertEquals(expected, HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf));

		// remapping
		// pointing to compound out
		srcString = "outerSeq/result";
		expected = "func7/result";
		assertEquals(expected, HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf));

		// pointing to compound in
		srcString = "parallel2/par2In1";
		expected = "func2/output";
		assertEquals(expected, HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf));

		// pointing to compound in
		srcString = "parallel2/res1";
		expected = "func5/out";
		assertEquals(expected, HierarchyLevellingAfcl.getSrcDataId(srcString, seqParWf));
	}
}
