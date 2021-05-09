package at.uibk.dps.ee.io.output;

import org.junit.Test;

import at.uibk.dps.ee.core.ExecutionData;
import at.uibk.dps.ee.core.ExecutionData.ResourceType;

public class ExcelPrinterTest {

  
  @Test
  public void test() {
    ExecutionData.startTimes.put("task1", 5L);
    ExecutionData.endTimes.put("task1", 5L);
    ExecutionData.resourceType.put("task1", ResourceType.IBM);
    ExecutionData.resourceRegion.put("task1", "eu-gb");
    ExecutionData.failRate = 0.5;
    ExecutionData.schedulingType = "Dynamic";
    ExcelPrinter.createExcelFile();
  }
}
