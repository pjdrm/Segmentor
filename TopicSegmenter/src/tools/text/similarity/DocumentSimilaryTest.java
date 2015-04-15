package tools.text.similarity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import tools.clustering.CorpusParser;
import tools.clustering.Document;
import tools.clustering.NgramGenerator;
import tools.clustering.SimpleTokenizer;
import tools.clustering.TFIDFWordFrequencyCounter;
import tools.clustering.Tokenizer;
import tools.text.similarity.SortableMap.ORDER;

public class DocumentSimilaryTest {

	public static void main(String[] args) {
		CorpusParser cp = new CorpusParser();
		String docsDir = args[0];
		List<Document> documents = cp.parse(docsDir);
		List<List<String>> tokenizedDocuments = new ArrayList<List<String>>();
		Tokenizer tokenizer = new SimpleTokenizer();
		Set<String> termSet = new LinkedHashSet<String>();
		List<String> tokens;
		System.out.println("Tokenizing documents");
		for(Document document : documents){
			tokens = NgramGenerator.getNGrams(1, tokenizer.tokenize(document.getText()));
			tokenizedDocuments.add(tokens);
			termSet.addAll(tokens);
		}
		
		System.out.println("Generating TFIDFWordFrequencyCounter");
		TFIDFWordFrequencyCounter fc = new TFIDFWordFrequencyCounter(tokenizedDocuments);
		Map<String, Double> docSimMap = new HashMap<String, Double>();
		try {
			double[] doc1;
			double[] doc2;
			for(int i = 0; i < documents.size(); i++){
				for(int j = 0; j < documents.size(); j++){
					if(i == j)
						continue;
					doc1 = fc.getFrequencyValues(tokenizedDocuments.get(i), termSet);
					doc2 = fc.getFrequencyValues(tokenizedDocuments.get(j), termSet);
					String[] splitArray = documents.get(j).getID().split("/");
					docSimMap.put(splitArray[splitArray.length-1], cosineSimilarity(doc1, doc2));
				}
				docSimMap = SortableMap.sort(docSimMap, ORDER.DESCENDING);
				String str = "";
				for(String key : docSimMap.keySet()){
					str += key + " " + docSimMap.get(key) + "\n";
				}
				
				FileUtils.write(new File("textSim/" + documents.get(i).getID()), str, false);
				docSimMap = new HashMap<String, Double>();
			}
			System.out.println("Finished generating document similarities");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static double cosineSimilarity(double[] xs, double[] ys) {
		double product = 0.0;
		double xsLengthSquared = 0.0;
		double ysLengthSquared = 0.0;
		for (int k = 0; k < xs.length; ++k) {
			xsLengthSquared += xs[k] * xs[k];
			ysLengthSquared += ys[k] * ys[k];
			product += xs[k] *  ys[k];
		}
		return product / Math.sqrt(xsLengthSquared * ysLengthSquared);
	}

}
