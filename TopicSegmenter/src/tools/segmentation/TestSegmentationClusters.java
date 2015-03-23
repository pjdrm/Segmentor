package tools.segmentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.LabelCell;
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

import org.apache.commons.io.FileUtils;

public class TestSegmentationClusters {

	public static void main(String[] args) {
		String clustersDir = args[0];
		String testDir = args[1];
		String configFile = args[2];
		List<String> clusterFiles = getClusterFiles(clustersDir);
		List<List<String>> clusters = new ArrayList<List<String>>();
		List<String> testFiles = new ArrayList<String>();
		for(String clusterFile : clusterFiles){
			List<String> cl = getCluster(clusterFile, clustersDir);
			clusters.add(cl);
			testFiles.addAll(cl);
		}
		TestScript.prepareScript(testDir+"/");
		TestScript.mkdir(testDir+"/combined", true);
		//TestScript.generateTestFiles(prune(clusters), testDir, testDir+"/combined");
		generateMissingFiles(prune(clusters), testDir, testDir+"/combined");
		initFileSizes(testDir);
		String[] tfArray = new File(testDir+"/combined").list();
		TestScript.testSegmentation(tfArray, configFile, testDir+"/combined");
		String outDir = "resultsClusters/" + TestScript.getAlgName(configFile);
		TestScript.printResults(outDir);
		TestScript.processResults(outDir);
		if(args.length == 4){
			String optimalResultFilePath = args[3];
			addOptimalResult(optimalResultFilePath, outDir+"/resultsProcessed.xls");
		}
		System.out.println("Finished segmentation tests based on clustering");
	}

	private static void addOptimalResult(String optimalResultFilePath, String resultsProcessedFilePath) {

		try {
			Map<String, String> optimalResultsMap = new HashMap<String, String>();
			Workbook workbook;
			workbook = Workbook.getWorkbook(new File(optimalResultFilePath));
			double bestWd = 1.0;
			double bestPk = 1.0;
			String bestFile = "";
			for(Sheet sheet : workbook.getSheets()){
				for(int lin = 1; lin < sheet.getRows(); lin++){
					NumberCell pkCell = (NumberCell)sheet.getCell(2,lin);
					NumberCell wdCell = (NumberCell)sheet.getCell(3,lin);
					LabelCell bestFileCell = (LabelCell)sheet.getCell(1,lin);
					if(wdCell.getValue() < bestWd){
						bestWd = wdCell.getValue();
						bestPk = pkCell.getValue();
						bestFile = bestFileCell.getContents();
					}
				}
				optimalResultsMap.put(sheet.getName(), bestFile + " " + String.valueOf(bestPk) + " " +  String.valueOf(bestWd));
				bestWd = 1.0;
				bestPk = 1.0;
				bestFile = "";
			}

			workbook = Workbook.getWorkbook(new File(resultsProcessedFilePath));
			WritableWorkbook copy = Workbook.createWorkbook(new File(resultsProcessedFilePath), workbook);
			int lin = copy.getSheet(0).getRows()+1;
			WritableCellFormat newFormat = new WritableCellFormat();
			newFormat.setAlignment(Alignment.CENTRE);
			for(WritableSheet sheet : copy.getSheets()){
				String[] optimalResults = optimalResultsMap.get(sheet.getName()).split(" ");
				sheet.addCell(new Label(1, lin, optimalResults[0], newFormat));
				sheet.addCell(new Number(2, lin, Double.parseDouble(optimalResults[1])));
				sheet.addCell(new Number(3, lin, Double.parseDouble(optimalResults[2])));
			}
			copy.write();
			copy.close();

		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void generateMissingFiles(List<List<String>> clusters,	String testDir, String outDir) {
		List<List<String>> missingClusters = new ArrayList<List<String>>();
		List<String> fullCluster = new ArrayList<String>();
		for(List<String> cl : clusters){
			for(String f : cl){
				fullCluster.add(f);
				if(! new File(testDir+"/combined/1_" + f).exists()){
					List<String> singleCl = new ArrayList<String>();
					singleCl.add(f);
					missingClusters.add(singleCl);
				}
			}
		}
		missingClusters.add(fullCluster);
		TestScript.generateTestFiles(missingClusters, testDir, testDir+"/combined");		
	}

	private static void initFileSizes(String testDir) {
		for(String tf : new File(testDir).list()){
			if(!new File(testDir + "/" + tf).isDirectory()){
				TestScript.fileSizeMap.put(tf, TestScript.countLines(testDir+"/"+tf, "UTF8"));
			}
		}		
	}

	private static List<List<String>> prune(List<List<String>> clusters) {
		List<List<String>> clustersPruned = new ArrayList<List<String>>();
		List<String> cluster = new ArrayList<String>();
		for(List<String> cl : clusters){
			for(String f : cl){
				String[] strSplit = f.split("/");
				cluster.add(strSplit[strSplit.length-1]);
			}
			clustersPruned.add(cluster);
			cluster = new ArrayList<String>();
		}
		return clustersPruned;
	}

	private static List<String> getCluster(String clusterFile, String clustersDir) {
		try {
			List<String> cluster = new ArrayList<String>();
			for(String line : FileUtils.readLines(new File(clustersDir + "/" + clusterFile))){
				if(line.startsWith("# ID=")){
					cluster.add((line.substring(5)));
				}
			}
			return cluster;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static List<String> getClusterFiles(String clustersDir) {
		File f = new File(clustersDir);
		List<String> clusterFiles = new ArrayList<String>();
		for(String file : f.list()){
			if(!new File(file).isDirectory() && file.startsWith("cluster_"))
				clusterFiles.add(file);
		}
		return clusterFiles;
	}

}
