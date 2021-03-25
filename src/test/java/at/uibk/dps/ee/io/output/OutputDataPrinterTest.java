package at.uibk.dps.ee.io.output;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import at.uibk.dps.ee.core.ExecutionData;

public class OutputDataPrinterTest {

	@Test
	public void test() {
		/*
		final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStreamCaptor));

		JsonObject testInput = new JsonObject();
		testInput.addProperty("Prop1", true);
		testInput.addProperty("Prop2", 3);

		OutputDataPrinter tested = new OutputDataPrinter();
		ExecutionData.data.add(1);
		tested.handleOutputData(testInput, ExecutionData.data);
		String expected = "Enactment finished\nEnactment result: " + testInput.toString() + "\n1";
		assertEquals(expected, outputStreamCaptor.toString().trim());
		*/
	}
}
