package tools.clustering;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ElkiFileGenerator {

	/**
	 * @param args
	 */
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
		int i = 0;
		String elkiFile = "";
		try {
			double[] vsmVect;
			String str;

			for(Document document : documents){
				str = "";
				System.out.println("Document " + document.getID());
				vsmVect = fc.getFrequencyValues(tokenizedDocuments.get(i++), termSet);
				for(int j = 0; j < vsmVect.length; j++){
					vsmVect[j] = (double)Math.round(vsmVect[j] * 1000) / 1000;
				}
				str = Arrays.toString(vsmVect);
				str = str.substring(1, str.length()-1);
				str = str.replaceAll(",", "");
				str = "ID=" + document.getID() + " " + str;
				elkiFile += str +"\n";
			}
			FileUtils.write(new File("clustering/elkiExcel.csv"), elkiFile, false);
			System.out.println("Finished generating elki excel files");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}