package tools.segmentation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import tools.text.similarity.SortableMap;
import tools.text.similarity.SortableMap.ORDER;
import edu.mit.nlp.MyTextWrapper;
import edu.mit.nlp.segmenter.SegTester;

public class LanguageModelAnalysis {

	public static void main(String[] args) {
		String filePath = args[0];
		List<String> segments = getSegments(filePath);
		int i = 0;
		
		try {
			String lms = "";
			//just to load stop words and stuff
			String[] argsSegTest = new String[]{"-config", "config/dp-mine.config", "-num-segs", "2"};
			TestScript.getSegTester(argsSegTest);
			for(String segment : segments){
				String tmpFile = "tmp/seg" + i + ".txt";
				FileUtils.write(new File(tmpFile), segment);
				Map<String, Double> wordCountMap = SortableMap.sort(createLanguageModel(tmpFile), ORDER.DESCENDING);
				for(String word : wordCountMap.keySet()){
					lms += word + " " + wordCountMap.get(word)+"\n";
				}
				i++;
				String segLMFile = filePath.split("/")[filePath.split("/").length-1].replaceAll("\\.txt", "txt") + "_seg"+i+".txt"; 
				File outFile = new File("langua_model_analysis/"+segLMFile);
				FileUtils.write(outFile, lms, false);
				lms = "";
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done generating LMs");
	}

	private static Map<String, Double> createLanguageModel(String filePath) {
		MyTextWrapper textwrapper = new MyTextWrapper(filePath);
		SegTester.preprocessText(textwrapper, true, false, true, true, 0);
		double[][] w = textwrapper.createWordOccurrenceTable(); //D x T matrix
		Map<String, Double> wordCountMap = new HashMap<String, Double>();
		for(int i = 0; i < textwrapper.getLexMap().getStemLexiconSize(); i++){
			wordCountMap.put(textwrapper.getLexMap().getStem(i), sumRow(i, w));
		}

		return wordCountMap;		
	}

	private static Double sumRow(int i, double[][] w) {
		double sum = 0.0;
		for(int j = 0; j < w[i].length; j++){
			sum += w[i][j];
		}
		return sum;
	}

	private static List<String> getSegments(String filePath) {
		try {
			List<String> lines = FileUtils.readLines(new File(filePath));
			List<String> segments = new ArrayList<String>();
			String currentSeg = "==========\n";
			for(int i = 1; i < lines.size(); i++){
				String lin = lines.get(i);
				if(lin.equals("==========")){
					segments.add(currentSeg+lin);
					currentSeg = "==========\n";
				}
				else{
					currentSeg += lin+"\n";
				}
			}
			return segments;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
