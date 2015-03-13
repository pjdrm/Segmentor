package tools.segmentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.LineChartSerie;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;


public class PlotCreator {

	private static List<String> algsNames = new ArrayList<String>();
	public static void main(String[] args) {
		/* Create a Workbook object that will hold the final chart */
		try {
			String filePath = args[0];
			File file = new File(filePath);
			InputStream st = new FileInputStream(file);
			XSSFWorkbook my_workbook = new XSSFWorkbook(st);
			for(XSSFSheet my_worksheet : my_workbook){
				createCharts(filePath, my_worksheet);
			}
			/* Finally define FileOutputStream and write chart information */               
			FileOutputStream fileOut = new FileOutputStream(filePath);
			my_workbook.write(fileOut);
			fileOut.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Finished generating plots");
	}

	public static void createCharts(String filePath, XSSFSheet worksheet){
		/* At the end of this step, we have a worksheet with test data, that we want to write into a chart */
		/* Create a drawing canvas on the worksheet */
		XSSFDrawing xlsx_drawing = worksheet.createDrawingPatriarch();
		/* Define anchor points in the worksheet to position the chart */
		XSSFClientAnchor anchorPk = xlsx_drawing.createAnchor(0, 0, 0, 0, 6, 1, 21, 20);
		XSSFClientAnchor anchorWd = xlsx_drawing.createAnchor(0, 0, 0, 0, 6, 22, 21, 41);
		/* Create the chart object based on the anchor point */
		XSSFChart chartPk = xlsx_drawing.createChart(anchorPk);
		XSSFChart chartWd = xlsx_drawing.createChart(anchorWd);
		/* Define legends for the line chart and set the position of the legend */
		XSSFChartLegend legendPk = chartPk.getOrCreateLegend();
		legendPk.setPosition(LegendPosition.RIGHT);
		
		XSSFChartLegend legendWd = chartWd.getOrCreateLegend();
		legendWd.setPosition(LegendPosition.RIGHT);     
		/* Create data for the chart */
		LineChartData dataPk = chartPk.getChartDataFactory().createLineChartData();
		LineChartData dataWd = chartWd.getChartDataFactory().createLineChartData();
		/* Define chart AXIS */
		ChartAxis bottomAxisPk = chartPk.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
		ValueAxis leftAxisPk = chartPk.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
		leftAxisPk.setCrosses(AxisCrosses.AUTO_ZERO);    
		
		ChartAxis bottomAxisWd = chartWd.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
		ValueAxis leftAxisWd = chartWd.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
		leftAxisWd.setCrosses(AxisCrosses.AUTO_ZERO);  
		/* Define Data sources for the chart */
		/* Set the right cell range that contain values for the chart */
		/* Pass the worksheet and cell range address as inputs */
		/* Cell Range Address is defined as First row, last row, first column, last column */
		List<Integer> ranges = getPlotRanges(worksheet);
		int prevRows = 1;
		int i = 0;
		for(Integer range : ranges){
			ChartDataSource<String> xsPk = DataSources.fromStringCellRange(worksheet, new CellRangeAddress(prevRows, range, 1, 1));
			ChartDataSource<Number> ysPk = DataSources.fromNumericCellRange(worksheet, new CellRangeAddress(prevRows, range, 2, 2));
			
			ChartDataSource<String> xsWd = DataSources.fromStringCellRange(worksheet, new CellRangeAddress(prevRows, range, 1, 1));
			ChartDataSource<Number> ysWd = DataSources.fromNumericCellRange(worksheet, new CellRangeAddress(prevRows, range, 3, 3));
			prevRows = range + 3;
			/* Add chart data sources as data to the chart */
			LineChartSerie chartSeriePk = dataPk.addSerie(xsPk, ysPk);
			chartSeriePk.setTitle(algsNames.get(i));
			
			LineChartSerie chartSerieWd = dataWd.addSerie(xsWd, ysWd);
			chartSerieWd.setTitle(algsNames.get(i++));			
		}
		
		/* Plot the chart with the inputs from data and chart axis */
		chartPk.plot(dataPk, new ChartAxis[] { bottomAxisPk, leftAxisPk });
		chartWd.plot(dataWd, new ChartAxis[] { bottomAxisWd, leftAxisWd });
	}

	private static List<Integer> getPlotRanges(XSSFSheet worksheet) {
		List<Integer> ranges = new ArrayList<Integer>();
		System.out.println(worksheet.getSheetName());
		algsNames.add(worksheet.getRow(0).getCell(1).getStringCellValue());
		for(int x = 1; x < worksheet.getLastRowNum(); x++){
			if(worksheet.getRow(x) != null && !worksheet.getRow(x).getCell(1).getStringCellValue().endsWith(".txt")){
				ranges.add(x-2);
				algsNames.add(worksheet.getRow(x).getCell(1).getStringCellValue());
			}
		}
		ranges.add(worksheet.getLastRowNum());
		//System.out.println(ranges);
		return ranges;
	}

}
