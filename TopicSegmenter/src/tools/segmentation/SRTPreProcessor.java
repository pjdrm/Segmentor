package tools.segmentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Pattern;

public class SRTPreProcessor {

	public static void main(String[] args) {
		BufferedReader br;
		Writer writer = null;
		String filePath = "data/MIT/2/MIT2_AVL.srt";
		String outPath = "data/MIT/2/MIT2_AVL_Processed.txt";
		String phrase = "";
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outPath)));
			br = new BufferedReader(
					   new InputStreamReader(
			                      new FileInputStream(filePath), "UTF8"));
			String line = br.readLine();
			Pattern pNumber = Pattern.compile("^[0-9]+$");
			Pattern pEmpty = Pattern.compile("^$");
			while (line != null) {
				if(pNumber.matcher(line).find()){

				}
				else if(line.contains("-->")){

				}
				else if(pEmpty.matcher(line).find()){

				}
				else{
					if(line.endsWith(".")){
						phrase += " " + line;
						writer.write(phrase.trim()+"\n");
						phrase = "";
					}
					else{
						phrase += " " + line;
					}
				}

				line = br.readLine();
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
		System.out.println("DONE");

	}

}
