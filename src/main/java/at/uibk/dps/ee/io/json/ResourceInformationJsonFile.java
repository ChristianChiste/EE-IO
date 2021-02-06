package at.uibk.dps.ee.io.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * The {@link ResourceInformationJsonFile} class models the information about
 * the resources which can be used for the enactment of the different resource
 * types.
 * 
 * @author Fedor Smirnov
 */
public class ResourceInformationJsonFile extends ArrayList<FunctionTypeEntry> {
	private static final long serialVersionUID = 1L;

	/**
	 * Converts the json file found in the provided path to a
	 * {@link ResourceInformationJsonFile}.
	 * 
	 * @param filePath the path to the json file
	 * @return the {@link ResourceInformationJsonFile} built from the file
	 */
	public static ResourceInformationJsonFile readFromFile(String filePath) {
		Gson gson = new Gson();
		String jsonString;
		try {
			jsonString = Files.readString(Paths.get(filePath));
			return gson.fromJson(jsonString, ResourceInformationJsonFile.class);
		} catch (IOException e) {
			throw new IllegalStateException("IOException when trying to read resource input.");
		}
	}
}
