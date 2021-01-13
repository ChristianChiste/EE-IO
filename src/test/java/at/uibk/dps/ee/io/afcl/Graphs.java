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
		return getWf(ConstantsTestCoreEEiO.cfclFileSingleAtomic);
	}

	public static Workflow getSeqParWf() {
		return getWf(ConstantsTestCoreEEiO.cfclFileSeqPar);
	}

	public static Workflow getIfWf() {
		return getWf(ConstantsTestCoreEEiO.cfclFileIf);
	}

	public static Workflow getElementIndexWf() {
		return getWf(ConstantsTestCoreEEiO.cfclFileElementIndex);
	}

	protected static Workflow getWf(String fileName) {
		try {
			byte[] data = UtilsSocket.readFileToBytes(fileName);
			return AfclReader.bytes2Workflow(data);
		} catch (IOException ioExc) {
			fail("IOException when getting the SeqPar workflow");
			return null;
		}
	}

}
