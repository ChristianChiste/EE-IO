package at.uibk.dps.ee.io.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.uibk.dps.ee.core.ExecutionData;
import at.uibk.dps.ee.core.ExecutionData.ResourceType;

/**
 * The {@link ExcelPrinter} creates an Excel(.xlsx) file using the data in {@link ExecutionData}.
 * 
 * @author Christian Chisté
 *
 */
public class ExcelPrinter {

  private final static String[] columns = {"taskId", "start", "end", 
      "resource","region", "failRate", "schedulingType", "workflowName", "timestamp"};

  public static void createExcelFile() {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Executions");
    Font headerFont = workbook.createFont();
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);

    Row headerRow = sheet.createRow(0);
    for(int i = 0; i < columns.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(columns[i]);
      cell.setCellStyle(headerCellStyle);
    }
    String timestamp = new Timestamp(System.currentTimeMillis()).toString();
    int rowNum = 1;
    for(String taskId : ExecutionData.startTimes.keys().uniqueSet()) {
      Iterator<Long> startTimes = ExecutionData.startTimes.get(taskId).iterator();
      Iterator<Long> endTimes = ExecutionData.endTimes.get(taskId).iterator();
      Iterator<ResourceType> resourceType = ExecutionData.resourceType.get(taskId).iterator();
      Iterator<String> resourceRegion = ExecutionData.resourceRegion.get(taskId).iterator();
      while(startTimes.hasNext()) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(taskId);
        row.createCell(1).setCellValue(startTimes.next());
        if(endTimes.hasNext())
          row.createCell(2).setCellValue(endTimes.next());
        else
          row.createCell(2).setCellValue(-1L);
        row.createCell(3).setCellValue(resourceType.next().toString());
        row.createCell(4).setCellValue(resourceRegion.next());
        row.createCell(5).setCellValue(ExecutionData.failRate);
        row.createCell(6).setCellValue(ExecutionData.schedulingType);
        row.createCell(7).setCellValue(ExecutionData.workflowName);
        row.createCell(8).setCellValue(timestamp);
      }
    }
    FileOutputStream fileOut;
    try {
      File directory = new File("executions");
      if (!directory.exists()) {
        directory.mkdir();
      }
      fileOut = new FileOutputStream("executions/" + ExecutionData.workflowName + "-" + 
    ExecutionData.schedulingType + "-" + ExecutionData.failRate + "-" + 
    timestamp + ".xlsx");
      workbook.write(fileOut);
      fileOut.close();
      workbook.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}