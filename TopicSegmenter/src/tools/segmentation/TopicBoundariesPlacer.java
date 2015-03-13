package tools.segmentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class TopicBoundariesPlacer {

	public static void main(String[] args) {
		String boundariesStr = args[0].substring(1, args[0].length()-1);
		String[] boundaries = boundariesStr.split(", ");
		placeBondaries(boundaries, args[1], "data/boundaries.txt");

	}
	
	public static void placeBondaries(String[] boundaries, String filePath, String outPath){
		int i = 0;
		int j = 0;
		
		BufferedReader br;
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outPath)));
			br = new BufferedReader(new FileReader(filePath));
			String line = br.readLine();
			writer.write("==========\n");
			while (line != null) {
				if(i < boundaries.length && Integer.parseInt(boundaries[i]) == j){
					writer.write("==========\n");
					i++;
				}
				writer.write(line+"\n");
				j++;
				line = br.readLine();
			}
			writer.write("==========");
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}
	
	public static void placeBondariesInt(List<Integer> boundaries, String filePath, String outPath){
		String[] boundariesStr = new String[boundaries.size()];
		int i  = 0;
		for(Integer boundInt : boundaries){
			boundariesStr[i++] = String.valueOf(boundInt);
		}
		placeBondaries(boundariesStr, filePath, outPath);
	}

}
