import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentVector {

	private File document;
	private ConcurrentHashMap<String, DocumentTerm> documentTerms;
	private double vectorNorm;
	
	public DocumentVector(File document){
		this.document = document;
		this.documentTerms = new ConcurrentHashMap<String, DocumentTerm>();
	}
	
	
	
	//getters and setters
	public ConcurrentHashMap<String, DocumentTerm> getDocumentTerms() {
		return documentTerms;
	}
	
	//purpose: Calculate vector norm for this document vector.
	//         Value used to calculate cosine between this vector and a query vector
	//paramters: none
	//returns: none
	public void calculateAndSetVectorNorm(){
		
		this.vectorNorm = 0;
		Iterator<String> keys = this.documentTerms.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next(); 
			this.vectorNorm += Math.pow(documentTerms.get(key).getDocumentTermWeight(), 2);
		}
		
		this.vectorNorm = Math.sqrt(this.vectorNorm);
	}
	
	public double getVectorNorm(){
			return this.vectorNorm;
	}
	
	public File getDocument(){
		return this.document;
	}
	
}
