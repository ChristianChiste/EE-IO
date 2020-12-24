package at.uibk.dps.ee.io.input;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.opt4j.core.start.Constant;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.InputDataProvider;

@Singleton
public class InputDataProviderFile implements InputDataProvider {

	protected final JsonObject inputData;

	@Inject
	public InputDataProviderFile(
			@Constant(value = "filePath", namespace = InputDataProviderFile.class) String filePath) {
		this.inputData = readInputData(filePath);
	}

	/**
	 * Reads the input data. Assumes that the data is annotated with type
	 * information.
	 * 
	 * @return the {@link Data} object containing the input data.
	 */
	protected JsonObject readInputData(String filePath) {
		return file2JsonObject(filePath);
	}

	/**
	 * Reads the file and returns the jsonobject found therein
	 * 
	 * @param filePath the path to the file
	 * @return the jsonobject found in the file
	 */
	protected JsonObject file2JsonObject(String filePath) {
		JsonElement result = null;
		try {
			result = JsonParser.parseReader(new FileReader(filePath));
		} catch (FileNotFoundException fnfE) {
			throw new IllegalArgumentException("File " + filePath + " not found");
		}

		if (!result.isJsonObject()) {
			throw new IllegalArgumentException("The file found under " + filePath + " does not contain a JSON object.");
		}
		return (JsonObject) result;
	}

	@Override
	public JsonObject getInputData() {
		return inputData;
	}
}
