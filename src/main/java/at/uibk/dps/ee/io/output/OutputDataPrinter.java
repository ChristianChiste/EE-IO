package at.uibk.dps.ee.io.output;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.OutputDataHandler;

/**
 * The {@link OutputDataPrinter} simply prints the enactment result to the
 * standard out.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class OutputDataPrinter implements OutputDataHandler {

  @Override
  public void handleOutputData(final JsonObject outputData, List<Integer> data) {
    System.out.println("Enactment finished");
    System.out.println("Enactment result: " + outputData.toString());
    System.out.println(data.get(0));
  }
}
