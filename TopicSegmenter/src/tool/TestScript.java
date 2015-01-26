package tool;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.mit.nlp.segmenter.SegTester;

public class TestScript {

	public static void main(String[] args) {
		prepareTestFiles();
		PrintStream origOut = System.out;
	    PrintStream interceptor = new Interceptor(origOut);
	    System.setOut(interceptor);
	    String testDirPath = "data/tests/combined";
	    
	    File testDir = new File(testDirPath);
	    String[] testFiles = testDir.list();
	    String results = "Results:\n";
	    //String[] argsSegTest = new String[]{"-config", "config/dp-mine.config", "-dir", testDirPath, "-suff", "2_AVL_gs_MIT_AVL_gs.txt"};
		//SegTester.main(argsSegTest);
	    for(int i = 0; i < testFiles.length; i++){
	    	String[] argsSegTest = new String[]{"-config", "config/dp-mine.config", "-dir", testDirPath, "-suff", testFiles[i]};
			SegTester.main(argsSegTest);
			results += testFiles[i] + "\t" + getLastLine(((Interceptor)interceptor).getPrints()) + "\n";
	    }
	    System.out.println(results);
	}
	
	private static String getLastLine(String str) {
		String[] strArray = str.split("\n");
		return strArray[strArray.length-1];
	}

	public static void prepareTestFiles(){
		String dirPath = "data/tests/";
		File f = new File(dirPath);

		File theDir = new File("data/tests/combined");


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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}

		/*List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		l2.add("1");
		l2.add("2");
		l2.add("3");
		l2.add("4");
		l2.add("5");
		System.out.println(getFileCOmbinations(l1, l2, 3));*/
		
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		String[] filesArray = f.list();
		for(int i = 0; i < f.list().length; i++){
			if(!new File(dirPath+filesArray[i]).isDirectory())
				l2.add(filesArray[i]);
		}
		
		for(int i = 1; i <= l2.size(); i++){
			List<List<String>> fileCombinations = getFileCOmbinations(l1, l2, i);
			//System.out.println(fileCombinations);
			generateTstFiles(fileCombinations, dirPath, "data/tests/combined");
		}
	}

	private static void generateTstFiles(List<List<String>> fileCombinations, String filesDir, String outDir) {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}

	private static String getCatFileName(List<String> filesToCat) {
		String name = "";
		for(String fileName : filesToCat){
			name += fileName.replaceAll("\\.txt", "")+"_";
		}
		return filesToCat.size()+"_"+name.substring(0, name.length()-1) + ".txt";
	}

	private static List<List<String>> getFileCOmbinations(List<String> l1, List<String> l2, int n_combs) {
		//System.out.println("L1: " + l1);
		//System.out.println("L2: " + l2);
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

	private static String[] filterDirs(String[] files, String baseDir) {
		List<String> filteredFiles = new ArrayList<String>();
		for(int i = 0; i < files.length; i++){
			if(new File(baseDir+files[i]).isDirectory())
				continue;
			filteredFiles.add(files[i]);
		}
		return filteredFiles.toArray(new String[filteredFiles.size()]);
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
