package tools.segmentation;

import java.util.ArrayList;
import java.util.List;

import edu.mit.nlp.MyTextWrapper;
import edu.mit.nlp.segmenter.SegTester;

public class SegmentationAnalysis {

	public static void main(String[] args) {
		String combinedFile = args[0];
		String configFile = args[1];
		String testDir = args[2];
		String tfArray[] = new String[]{combinedFile};
		TestScript.initFileSizes(testDir);
		TestScript.disableCache();
		TestScript.prepareScript(testDir);
		TestScript.testSegmentation(tfArray, configFile, testDir+"/combined");
		System.out.println("Finished generating files for analysis");
		//sanatyChek();
	}
	
	public static void sanatyChek(){
		
		SegTester segTester = null;
		String[] argsSegTest = new String[]{"-config", "config/dp-mine.config", "-num-segs", "2"};
		try {
			segTester = TestScript.getSegTester(argsSegTest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		List[] hyp_segs = new List[1];
		List<Integer> sanityCheckBoundaries = new ArrayList<Integer>();
		/*sanityCheckBoundaries.add(10);
		sanityCheckBoundaries.add(12);
		sanityCheckBoundaries.add(14);
		sanityCheckBoundaries.add(19);
		sanityCheckBoundaries.add(25);
		sanityCheckBoundaries.add(41);
		sanityCheckBoundaries.add(61);*/
		sanityCheckBoundaries.add(69);
		hyp_segs[0] = sanityCheckBoundaries;
		
		MyTextWrapper[] textWrapper = new MyTextWrapper[1];
		String t = "data/tests2/gs/AVL_wiki_gs.txt";
		textWrapper[0] = segTester.loadText(t);
		segTester.eval(hyp_segs, textWrapper);
	}

}
