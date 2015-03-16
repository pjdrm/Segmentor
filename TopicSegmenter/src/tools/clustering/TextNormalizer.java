package tools.clustering;

public class TextNormalizer {


	public static String normDMarks(String text){
		return NormalizerSimple.normDMarks(text).replaceAll("[ºª€]", "");
	}

	public static String weirdChars(String text){
		return text.replaceAll("[ºª€*]", "");
	}

}
