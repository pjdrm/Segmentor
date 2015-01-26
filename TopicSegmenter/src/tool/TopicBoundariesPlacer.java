package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TopicBoundariesPlacer {

	public static void main(String[] args) {
		String boundariesStr = args[0].substring(1, args[0].length()-1);
		String[] boundaries = boundariesStr.split(", ");
		int i = 0;
		int j = 0;
		
		BufferedReader br;
		Writer writer = null;
		String filePath = args[1];
		String outPath = "data/boundaries.txt";
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outPath)));
			br = new BufferedReader(new FileReader(filePath));
			String line = br.readLine();
			
			while (line != null) {
				if(i < boundaries.length && Integer.parseInt(boundaries[i]) == j){
					writer.write("==========\n");
					i++;
				}
				writer.write(line+"\n");
				j++;
				line = br.readLine();
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}

	}

}
