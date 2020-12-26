package at.uibk.dps.ee.io.testconstants;

/**
 * Container for the constants used within the tests.
 * 
 * @author Fedor Smirnov
 *
 */
public class ConstantsTestCoreEEiO {

	// constants for the afcl test input
	public static final String cfclFileSingleAtomic = "src/test/resources/wfInputFiles/singleAtomic.yaml";

	public static final String wfNameAtomic = "single Atomic";
	public static final String inputNameAtomic = "input_name";
	public static final String outputNameAtomic = "output_name";
	public static final String wfInputJsonNameAtomic = "inputSource";
	public static final String wfFunctionNameAtomic = "atomicFunction";
	public static final String wfFunctionInputNameAtomic = "myInput";
	public static final String wfFunctionOutputNameAtomic = "myOutput";
	public static final String wfFunctionResourceNameAtomic = "my_res_link";

	// constants for the read/write test
	public static final String xmlFileTestReadWrite = "src/test/resources/xmlFiles/testAtomicWrite.xml";
	public static final String xmlFileTestAtomic = "src/test/resources/xmlFiles/testAtomic.xml";
	public static final String jsonInputFile = "src/test/resources/testInput/testInput.json";
	public static final String jsonInputFileWrong = "src/test/resources/testInput/testInputWrong.json";
}
