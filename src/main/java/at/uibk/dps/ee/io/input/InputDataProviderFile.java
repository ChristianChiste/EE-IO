package at.uibk.dps.ee.io.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opt4j.core.start.Constant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.InputDataProvider;

/**
 * The {@link InputDataProviderFile} is used to read the input data from a file
 * in the storage.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class InputDataProviderFile implements InputDataProvider {

  protected final JsonObject inputData;

  /**
   * Injection constructor
   * 
   * @param filePath the path to the .json file containing the input data
   */
  @Inject
  public InputDataProviderFile(@Constant(value = "filePath",
      namespace = InputDataProviderFile.class) final String filePath) {
    this.inputData = file2JsonObject(filePath);
  }

  /**
   * Reads the file and returns the jsonobject found therein
   * 
   * @param filePath the path to the file
   * @return the jsonobject found in the file
   */
  protected final JsonObject file2JsonObject(final String filePath) {
    try {
      return (JsonObject) JsonParser.parseReader(Files.newBufferedReader(Paths.get(filePath)));
    } catch (IOException ioExc) {
      throw new IllegalArgumentException("IO Exception when trying to read file " + filePath,
          ioExc);
    }
  }

  @Override
  public JsonObject getInputData() {
    return inputData;
  }
}
