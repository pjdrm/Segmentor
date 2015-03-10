package tool;

import java.io.File;
import java.io.IOException;

import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number;
import jxl.NumberCell;

public class ResultsMerger {

	public static void main(String[] args) {
		String resutsDirPath = "results/";
		File resultsDir = new File(resutsDirPath);
		try {
			WritableWorkbook allResultsExcel = Workbook.createWorkbook(new File("allResults.xls"));		
			int linIncr = initExcel(allResultsExcel, resutsDirPath + resultsDir.list()[0]+"/resultsProcessed.xls");
			int line = 0;
			for(String algResultsDir : resultsDir.list()){
				addResults(allResultsExcel, Workbook.getWorkbook(new File(resutsDirPath + algResultsDir +"/resultsProcessed.xls")), algResultsDir, line);
				line += linIncr + 1;
			}

			allResultsExcel.write(); 
			allResultsExcel.close();
			System.out.println("Finished merging results");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void addResults(WritableWorkbook allResultsExcel, Workbook resultsExcel, String algName, int line) {
		try {
			int k = 0;
			int i = line;
			WritableCellFormat newFormat = new WritableCellFormat();
			newFormat.setAlignment(Alignment.CENTRE);
			for(Sheet resultsSheet : resultsExcel.getSheets()){
				
				WritableSheet allResultsSheet = allResultsExcel.getSheet(k++);
				CellView cell = allResultsSheet.getColumnView(1);
				cell.setAutosize(true);
				allResultsSheet.setColumnView(1, cell);
				
				allResultsSheet.addCell(new Label(1, i, algName, newFormat));
				allResultsSheet.addCell(new Label(2, i, "Pk", newFormat));
				allResultsSheet.addCell(new Label(3, i++, "Wd", newFormat));
				for(int x = 1; x < resultsSheet.getRows(); x++){
					allResultsSheet.addCell(new Label(1, i, resultsSheet.getCell(1, x).getContents(), newFormat));
					allResultsSheet.addCell(new Number(2, i, ((NumberCell)resultsSheet.getCell(2, x)).getValue(), newFormat));
					allResultsSheet.addCell(new Number(3, i++, ((NumberCell)resultsSheet.getCell(3, x)).getValue(), newFormat));
				}
				i = line;
			}
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static int initExcel(WritableWorkbook writableWorkbook, String excelPath) {
		try {
			Workbook workbook = Workbook.getWorkbook(new File(excelPath));
			int i = 0;
			for(String sheetName : workbook.getSheetNames()){
				writableWorkbook.createSheet(sheetName, i++);
			}
			return workbook.getSheets()[0].getRows();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

}
