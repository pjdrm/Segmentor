package tool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import ml.options.Options;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import edu.mit.nlp.MyTextWrapper;
import edu.mit.nlp.segmenter.SegTester;

public class TestScript {

	private static Map<String, Integer> fileSizeMap = new HashMap<String, Integer>();
	private static Map<String, Map<String, List<Double>>> resultsMap = new LinkedHashMap<String, Map<String, List<Double>>>();
	private static String testBaseDir = "data/tests2/";
	private static String testDirPath = testBaseDir + "combined";
	private static String segDirPath = testBaseDir + "segmentations";

	public static void main(String[] args) {
		
		prepareTestFiles(testBaseDir);

		PrintStream origOut = System.out;
		PrintStream interceptor = new Interceptor(origOut);
		System.setOut(interceptor);

		File testDir = new File(testDirPath);
		String[] testFiles = testDir.list();
		initResultsMap(testFiles);
		String[] argsSegTest = new String[]{"-config", "config/dp-mine.config", "-num-segs", "2"};
		SegTester segTester = null;
		try {
			segTester = getSegTester(argsSegTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for(int i = 0; i < testFiles.length; i++){

			InputStream testInput;
			List[] hyp_segs = new List[1];
			try {
				String data = FileUtils.readFileToString(new File(testDirPath + "/" + testFiles[i]));
				testInput = new ByteArrayInputStream(data.getBytes("UTF-8"));
				System.setIn(testInput);
				SegTester.main(argsSegTest);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<String> files = getIndividualFiles(testFiles[i]);
			List<List<Integer>> individualBoundaries = getIndividualBoundaries(files, segTester.getBoundaries());
			int j = 0;
			List<Integer> boundariesInt;
			for(String individualFile : files){
				MyTextWrapper[] textWrapper = new MyTextWrapper[1];
				String t = testBaseDir + "gs/" + individualFile.split("\\.")[0] + "_gs.txt";
				System.out.println("Debug: " + testFiles[i]);
				textWrapper[0] = segTester.loadText(t);
				boundariesInt = individualBoundaries.get(j++);
				TopicBoundariesPlacer.placeBondariesInt(boundariesInt, testBaseDir+individualFile, segDirPath+"/"+ individualFile.split("\\.")[0] + "#" + testFiles[i]);
				hyp_segs[0] = boundariesInt;
				segTester.eval(hyp_segs, textWrapper);
				saveResult(individualFile, testFiles[i], getLastLine(((Interceptor)interceptor).getPrints()));
			}

		}
		printResults();
		processResults();
		System.out.println("Finished testing");
	}

	private static void printResults() {
		Workbook workbook;
		WritableWorkbook writableWorkbook = null;
		WritableSheet sheet = null;
		String fileName = "results.xls";
		new File(fileName).delete();
		try {
			workbook = Workbook.getWorkbook(new File(fileName));
			writableWorkbook = Workbook.createWorkbook(new File(fileName), workbook);
			sheet = writableWorkbook.getSheet(0);
		} catch (Exception e1) {
			try {
				writableWorkbook = Workbook.createWorkbook(new File(fileName));
				sheet = writableWorkbook.createSheet("results", 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int col = 1;
		int lin = 1;
		try {
			WritableCellFormat newFormat = new WritableCellFormat();
			newFormat.setAlignment(Alignment.CENTRE);
			for(String individualFile : resultsMap.keySet()){
				for(String testFile : resultsMap.get(individualFile).keySet()){
					CellView cell = sheet.getColumnView(col);
					cell.setAutosize(true);
					sheet.setColumnView(col, cell);
					sheet.addCell(new Label(col, lin, testFile, newFormat));
					sheet.addCell(new Label(col+1, lin, "Pk", newFormat));
					sheet.addCell(new Label(col+1, lin+1, "Wd", newFormat));
					lin += 2;
				}
				col += 2;
				lin = 0;
				break;
			}

			for(String individualFile : resultsMap.keySet()){
				CellView cell = sheet.getColumnView(col);
				cell.setAutosize(true);
				sheet.setColumnView(col, cell);
				sheet.addCell(new Label(col, lin++, individualFile, newFormat));
				for(String testFile : resultsMap.get(individualFile).keySet()){
					List<Double> results = resultsMap.get(individualFile).get(testFile);
					if(results != null){
						sheet.addCell(new Number(col, lin++, results.get(0), newFormat));
						sheet.addCell(new Number(col, lin++, results.get(1), newFormat));
					}
					else{
						sheet.addCell(new Label(col, lin++, "-", newFormat));
						sheet.addCell(new Label(col, lin++, "-", newFormat));
					}
				}
				col++;
				lin = 0;
			}

			writableWorkbook.write(); 
			writableWorkbook.close();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void processResults(){
		try {
			WritableWorkbook writableWorkbook = null;
			WritableSheet sheetNew = null;
			String fileName = "resultsProcessed.xls";
			new File(fileName).delete();
			writableWorkbook = Workbook.createWorkbook(new File(fileName));
			WritableCellFormat newFormat = new WritableCellFormat();
			newFormat.setAlignment(Alignment.CENTRE);
			
			Workbook workbook = Workbook.getWorkbook(new File("results.xls"));
			Sheet sheet = workbook.getSheet(0);
			int writeLin = 1;
			for(int col = 3; col < sheet.getColumns(); col++){
				sheetNew = writableWorkbook.createSheet(sheet.getCell(col, 0).getContents(), 0);
				sheetNew.addCell(new Label(2, 0, "Pk"));
				sheetNew.addCell(new Label(3, 0, "Wd"));
				System.out.println(sheet.getCell(col, 0).getContents());
				writeLin = 1;
				for(int lin = 1; lin < sheet.getRows(); lin = lin + 2){
					Cell resultCell = sheet.getCell(col,lin);
					if (resultCell.getType() == CellType.NUMBER){ 
						CellView cell = sheetNew.getColumnView(1);
						cell.setAutosize(true);
						sheetNew.setColumnView(1, cell);
						sheetNew.addCell(new Label(1, writeLin, sheet.getCell(1, lin).getContents(), newFormat));
						NumberCell nc = (NumberCell) resultCell; 
						sheetNew.addCell(new Number(2, writeLin, nc.getValue()));
						resultCell = sheet.getCell(col,lin+1);
						nc = (NumberCell) resultCell;
						sheetNew.addCell(new Number(3, writeLin++, nc.getValue()));
					}
					
				}
			}
			
			writableWorkbook.write(); 
			writableWorkbook.close();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void saveResult(String individualFile, String testFile, String resultsStr) {
		String[] resultsArray = resultsStr.split(" ");
		Double pk = Double.parseDouble(resultsArray[0].replaceAll(",", "."));
		Double wd = Double.parseDouble(resultsArray[1].replaceAll(",", "."));
		List<Double> results = new ArrayList<Double>();
		results.add(pk);
		results.add(wd);
		resultsMap.get(individualFile).put(testFile, results);		
	}

	private static void initResultsMap(String[] testFiles) {
		for(int i = 0; i < testFiles.length; i++){
			if(testFiles[i].startsWith("1_")){
				String key = testFiles[i].split("1_")[1];
				resultsMap.put(key, new LinkedHashMap<String, List<Double>>());
				for(int j = 0; j < testFiles.length; j++){
					resultsMap.get(key).put(testFiles[j], null);
				}	
			}
		}
	}

	private static List<List<Integer>> getIndividualBoundaries(List<String> files, List<Integer> boundaries) {
		if(files.size() == 3)
			System.out.println("");
		List<List<Integer>> indBoundaries = new ArrayList<List<Integer>>();
		List<Integer> indBound = new ArrayList<Integer>();
		List<Integer> fileSizes = new ArrayList<Integer>();
		for(String file : files){
			fileSizes.add(fileSizeMap.get(file));
		}

		int i = 0;
		int discount = 0;
		for(Integer boundary : boundaries){
			if(boundary - discount == fileSizes.get(i)){
				indBound.add(boundary - discount);
				indBoundaries.add(indBound);
				indBound = new ArrayList<Integer>();
				discount += fileSizes.get(i++);
			}
			else if(boundary - discount > fileSizes.get(i)){
				indBound.add(fileSizes.get(i));
				indBoundaries.add(indBound);
				indBound = new ArrayList<Integer>();
				discount += fileSizes.get(i++);
				indBound.add(boundary - discount);
			}
			else{
				indBound.add(boundary - discount);
			}
		}
		if(files.size() > indBoundaries.size())
			indBoundaries.add(indBound);
		return indBoundaries;
	}

	private static List<String> getIndividualFiles(String catFile) {
		String[] files = catFile.split("[0-9]+_", 2)[1].split("-");
		List<String> indFiles = new ArrayList<String>();
		for(int i = 0; i < files.length-1; i++){
			indFiles.add(files[i]+".txt");
		}
		indFiles.add(files[files.length-1]);
		return indFiles;
	}

	public static SegTester getSegTester(String[] args) throws Exception{
		Options options = new Options(args);
		options.addSet("eval",0);
		options.getSet("eval").addOption("config",Options.Separator.BLANK,Options.Multiplicity.ONCE);
		options.getSet("eval").addOption("dir",Options.Separator.BLANK,Options.Multiplicity.ONCE);
		options.getSet("eval").addOption("suff",Options.Separator.BLANK,Options.Multiplicity.ONCE);
		options.getSet("eval").addOption("out",Options.Separator.BLANK,Options.Multiplicity.ZERO_OR_ONE);        
		options.getSet("eval").addOption("init",Options.Separator.BLANK,Options.Multiplicity.ZERO_OR_ONE);
		options.getSet("eval").addOption("debug",Options.Multiplicity.ZERO_OR_ONE);

		options.addSet("run",0);
		options.getSet("run").addOption("debug",Options.Multiplicity.ZERO_OR_ONE);
		options.getSet("run").addOption("num-segs",Options.Separator.BLANK,Options.Multiplicity.ZERO_OR_ONE);
		options.getSet("run").addOption("config",Options.Separator.BLANK,Options.Multiplicity.ZERO_OR_ONE);


		ml.options.OptionSet optset = options.getMatchingSet();
		return new SegTester(optset);
	}

	private static String getLastLine(String str) {
		String[] strArray = str.split("\n");
		return strArray[strArray.length-1];
	}

	public static void prepareTestFiles(String dirPath){
		File f = new File(dirPath);

		File theDir = new File(dirPath + "/combined");
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			try{
				theDir.mkdir();
			} catch(SecurityException se){
				se.printStackTrace();
			}  
		}
		else{
			try {
				FileUtils.cleanDirectory(theDir);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		theDir = new File(segDirPath);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			try{
				theDir.mkdir();
			} catch(SecurityException se){
				se.printStackTrace();
			}  
		}
		else{
			try {
				FileUtils.cleanDirectory(theDir);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		String[] filesArray = f.list();
		for(int i = 0; i < f.list().length; i++){
			if(!new File(dirPath+filesArray[i]).isDirectory()){
				l2.add(filesArray[i]);
				fileSizeMap.put(filesArray[i], countLines(dirPath+filesArray[i], "UTF8"));
			}
		}

		for(int i = 1; i <= l2.size(); i++){
			List<List<String>> fileCombinations = getFileCOmbinations(l1, l2, i);
			generateTestFiles(fileCombinations, dirPath, dirPath + "/combined");
		}
	}

	private static void generateTestFiles(List<List<String>> fileCombinations, String filesDir, String outDir) {
		int j = 0;
		for(List<String> filesToCat : fileCombinations){
			String fileName = getCatFileName(filesToCat);
			for(String fileToCat : filesToCat){
				// Files to read
				File file1 = new File(filesDir+"/"+fileToCat);
				String file1Str;
				try {
					file1Str = FileUtils.readFileToString(file1)+"\n";
					// File to write
					File file2 = new File(outDir + "/" + fileName);
					j++;
					// Write the file
					FileUtils.write(file2, file1Str, true); // true for append
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
	}

	private static String getCatFileName(List<String> filesToCat) {
		String name = "";
		for(String fileName : filesToCat){
			name += fileName.replaceAll("\\.txt", "")+"-";
		}
		return filesToCat.size()+"_"+name.substring(0, name.length()-1) + ".txt";
	}

	private static List<List<String>> getFileCOmbinations(List<String> l1, List<String> l2, int n_combs) {
		List<List<String>> combinations = new ArrayList<List<String>>();
		if(l1.size() == n_combs){
			combinations.add(l1);
		}
		else{
			for(int i = 0;  i < l2.size(); i++){
				List<String> newL1 = new ArrayList<String>();
				for(String s : l1)
					newL1.add(new String(s));
				newL1.add(new String(l2.get(i)));
				List<String> newL2 = new ArrayList<String>();
				for(int j = i+1; j < l2.size(); j++)
					newL2.add(new String(l2.get(j)));
				combinations.addAll(getFileCOmbinations(newL1, newL2, n_combs));
			}
		}
		return combinations;
	}

	public static int countLines(String filePath, String encoding) {
		File file = new File(filePath);
		int lines = 0;
		LineIterator lineIterator = null;
		try {
			lineIterator = FileUtils.lineIterator(file, encoding);
			while ( lineIterator.hasNext() ) {
				lines++;
				lineIterator.nextLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			LineIterator.closeQuietly( lineIterator );
		}
		return lines;
	}

	private static class Interceptor extends PrintStream {
		public String prints = "";
		public Interceptor(OutputStream out){
			super(out, true);
		}

		public String getPrints(){
			String tmp = new String(prints);
			prints = "";
			return tmp;
		}

		@Override
		public void print(String s){
			super.print(s);
			prints += s + "\n"; 
		}
	}

}
