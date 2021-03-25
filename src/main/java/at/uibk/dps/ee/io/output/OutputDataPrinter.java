package at.uibk.dps.ee.io.output;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.OutputDataHandler;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceResourceServerless;

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
  public void handleOutputData(final JsonObject outputData, Map<String,Long> startTimes, Map<String,Long> endTimes) {
    System.out.println("Enactment finished");
    System.out.println("Enactment result: " + outputData.toString());
    for(String taskId: startTimes.keySet()) {
      System.out.println(taskId + ": " + (endTimes.get(taskId) - startTimes.get(taskId)) * Math.exp(-9));
    }
    //excelPrinter.createExcelFile(startTimes, endTimes);
  }
}
