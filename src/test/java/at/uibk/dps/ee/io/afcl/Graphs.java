package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.fail;

import java.io.IOException;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;
import at.uibk.dps.socketutils.UtilsSocket;

/**
 * Convenience class to generate the test graphs.
 * 
 * @author Fedor Smirnov
 *
 */
public class Graphs {

	private Graphs() {
	}
	
	public static Workflow getSingleAtomicWf() {
		try {
			byte[] data = UtilsSocket.readFileToBytes(ConstantsTestCoreEEiO.cfclFileSingleAtomic);
			return AfclReader.bytes2Workflow(data);
		} catch (IOException ioExc) {
			fail("IOException in the testBytes2Wf test");
			return null;
		}
	}

	
}
