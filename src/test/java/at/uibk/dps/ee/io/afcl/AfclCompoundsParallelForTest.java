package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

public class AfclCompoundsParallelForTest {

	@Test
	public void testIsIntIterator() {
		assertTrue(AfclCompoundsParallelFor.isIntIterator("5"));
		assertTrue(AfclCompoundsParallelFor.isIntIterator("function/output"));
		assertFalse(AfclCompoundsParallelFor.isIntIterator("myDataIn"));
	}
}
