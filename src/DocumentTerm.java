import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentTerm implements Comparable<DocumentTerm>{

	//fields
	private File document;
	private ConcurrentHashMap<Integer, Boolean> termLocations;
	//NEW FOR EXTENDED BOOL
	private String keyword;
	private double documentTermWeight;
	
	//constructors
	public DocumentTerm(File doc, String keyword){
		this.document = doc;
		this.keyword = keyword;
		this.termLocations = new ConcurrentHashMap<Integer,Boolean>();
		this.documentTermWeight = 0;
	}
	
	public DocumentTerm(File doc, double cosine){
		this.document = doc;
		this.documentTermWeight = cosine;
	}
	
	//NEW FOR EXTENDED BOOL
	//purpose: calculate and store TF-IDF for this docuemtn term instance
	//paramters: double idf (inverse document frequency)
	//returns: none
	public void calculateAndSetDocumentTermWeight(double idf){
		this.documentTermWeight = calculateTermFrequency()*idf;
	}
	
	//NEW FOR EXTENDED BOOL
	//purpose: calculate term frequency using the number of termLoctions
	//paramters: none
	//returns: double termFrequency (1+log2(fij))
	public double calculateTermFrequency(){
		return 1 + Math.log10(termLocations.size())/Math.log10(2);
	}
	

	//getters and setters
	//NEW FOR EXTENDED BOOL
	public String getKeyword() {
		return keyword;
	}

	public double getDocumentTermWeight(){
		return this.documentTermWeight;
	}
	
	public void setDocumentTermWeight(double tw){
		this.documentTermWeight = tw;
	}
	
	public File getDocument() {
		return document;
	}

	public ConcurrentHashMap<Integer, Boolean> getTermLocations() {
		return termLocations;
	}

	@Override
	public boolean equals(Object d2){
		return this.getDocument().getName().equals(((DocumentTerm) d2).getDocument().getName());
	}
	
	@Override
	public int compareTo(DocumentTerm d2){
		double docWeight1 = this.documentTermWeight; 
		double docWeight2 = d2.getDocumentTermWeight(); 
		
		if(docWeight1 > docWeight2)
			return -1;
		if(docWeight1 < docWeight2)
			return 1;
		return 0;

	}
}
