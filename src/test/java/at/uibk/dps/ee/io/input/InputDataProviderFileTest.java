package at.uibk.dps.ee.io.input;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonObject;

import at.uibk.dps.ee.io.testconstants.ConstantsTestCoreEEiO;

public class InputDataProviderFileTest {

	@Test
	public void test() {
		InputDataProviderFile tested = new InputDataProviderFile(ConstantsTestCoreEEiO.jsonInputFile);
		JsonObject result = tested.getInputData();
		assertTrue(result.has("a"));
		assertTrue(result.has("b"));
		assertTrue(result.has("wait"));
		assertEquals(3, result.get("a").getAsInt());
		assertEquals(17, result.get("b").getAsInt());
		assertEquals("no", result.get("wait").getAsString());
	}
}
