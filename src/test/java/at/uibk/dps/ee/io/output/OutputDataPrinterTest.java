package at.uibk.dps.ee.io.output;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.google.gson.JsonObject;

public class OutputDataPrinterTest {

	@Test
	public void test() {
		final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStreamCaptor));

		JsonObject testInput = new JsonObject();
		testInput.addProperty("Prop1", true);
		testInput.addProperty("Prop2", 3);

		OutputDataPrinter tested = new OutputDataPrinter();
		tested.handleOutputData(testInput);
		String expected = "Enactment finished\nEnactment result: " + testInput.toString();
		assertEquals(expected, outputStreamCaptor.toString().trim());
	}
}
