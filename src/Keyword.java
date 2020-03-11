import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Keyword implements Comparable<Keyword>, QueryComponent {
	
	private String keyword;
	//count of keyword in all the documents
	private int count;
	private ConcurrentHashMap<String, DocumentTerm> documentTerms;
	
	//NEW FOR EXTENDED BOOL
	private double inverseDocumentFrequency;
	
	//constructor
	public Keyword(String keyword){
		this.keyword = keyword;
		this.count = 1;
		this.documentTerms = new ConcurrentHashMap<String, DocumentTerm>();
		this.inverseDocumentFrequency = 0;
	}
	
	//getters and setters
	
	//NEW FOR EXTENDED BOOL
	//purpose: calculate inverse document frequency
	//paramters: int N (the total number of documents in the collection) 
	//returns: none
	public void calculateAndSetInverseDocumentFrequency(int N){
		this.inverseDocumentFrequency = Math.log10(N/documentTerms.size())/Math.log10(2);
	}
	
	//NEW FOR EXTENDED BOOL
	//purpose: calculate and set all document term weights (tf-idf) for this keyword
	//paramters: none
	//returns: none
	public void calculateAndSetAllDocumentTermWeights(){
		
		Iterator<String> keys = documentTerms.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next(); 
			DocumentTerm doc = documentTerms.get(key);
			doc.calculateAndSetDocumentTermWeight(this.inverseDocumentFrequency);
		}	
	}
	
	public double getInverseDocumentFrequency(){
		return inverseDocumentFrequency;
	}
	
	public String getKeyword() {
		return keyword;
	}

	public int getCount() {
		return count;
	}

	public void increaseCountByOne() {
		this.count++;
	}

	
	public ConcurrentHashMap<String, DocumentTerm> getDocumentTerms() {
		return documentTerms;
	}
	

	public void setDocumentTerms(ConcurrentHashMap<String, DocumentTerm> documentTerms) {
		this.documentTerms = documentTerms;
	}


	@Override
	public boolean equals(Object k2){
		return this.getKeyword().equals(((Keyword) k2).getKeyword());
	}
	
	@Override
	public int compareTo(Keyword k2){
		return this.getKeyword().compareTo(k2.getKeyword());
	}
}
