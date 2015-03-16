package tools.clustering;

public class Document {

	private String text;
	private String id;
	
	public Document(String text, String id){
		this.text = text;
		this.id = id;
	}
	
	public String getText() {
		return text;
	}

	public String getID() {
		return id;
	}

}
