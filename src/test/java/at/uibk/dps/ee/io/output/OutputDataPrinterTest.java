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
		ExecutionData.startTimes.put("task1", 1000L);
		ExecutionData.endTimes.put("task1", 2000L);
		tested.handleOutputData(testInput);
		String expected = "Enactment finished\nEnactment result: " + testInput.toString();
		assertEquals(expected, outputStreamCaptor.toString().trim().substring(0,61));
		*/
	}
}
