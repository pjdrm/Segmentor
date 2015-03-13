package tools.segmentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.SlideShow;

public class SlideProcessor {

	public static void main(String[] args) {
		try {
			InputStream fis= new FileInputStream("lecture08.ppt");
			HSLFSlideShow show=new HSLFSlideShow(fis);
			SlideShow ss=new SlideShow(show);
			Slide[] slides=ss.getSlides();
			StringBuilder builder = new StringBuilder();

			for(int i = 0; i < slides.length; i++){
				TextRun[] runs = slides[i].getTextRuns();
				for(int j = 0; j < runs.length; j++) {
					TextRun run = runs[j];
					if(run != null) {
						String text = run.getText();
						if(text.length() == 1)
							continue;
						text = text.replaceAll("^ +", "");
						builder.append(text + "\n");
					}
				}
				builder.append("\n");
			}
			FileUtils.write(new File("lecture08.txt"), builder.toString(), false);


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
