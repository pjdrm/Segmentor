package tools.clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CorpusParser {

	public List<Document> parse(String docsDir) {
		List<Document> docs = new ArrayList<Document>();
		File f = new File(docsDir);
		for(String docPath : f.list()){
			if(docPath.endsWith("txt")){
				docs.add(parseDocument(docsDir+ "/" + docPath));
			}
		}
		
		return docs;
	}

	private Document parseDocument(String docPath) {
		String strDoc = null;
		try {
			strDoc = simpleNormalization(FileUtils.readFileToString(new File(docPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Document(strDoc, docPath);
	}
	
	private String simpleNormalization(String text){
		text = text.replaceAll("[\t\n\r]+", " ");
		text = TextNormalizer.normDMarks(text);
		text = TextNormalizer.weirdChars(text);
		//text = normalizer.modify(text);
		text = removePunct(text);
		text = text.replaceAll(" +", " ");
		return text;
	}
	
	private String removePunct(String text) {
		return text.replaceAll("[\\\\!$%&'\\(\\)\\*\\+,-\\./:;<=>\\?@\\[\\]\\^_`\\{\\|\\}~”\\\"]", "").toLowerCase();
	}

}
