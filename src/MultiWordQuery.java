import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

public class MultiWordQuery implements QueryComponent{
	private ArrayList<Keyword> queryTerms;
	private Indicies indicies;
	private ConcurrentHashMap <String,Double> queryVector;
	private double vectorNorm;
	private ConcurrentHashMap<String, DocumentTerm> documentCosines;
	
	
	public MultiWordQuery(ArrayList<Keyword> queryTerms, Indicies indicies){
		this.queryTerms = queryTerms;
		this.indicies = indicies;
		this.queryVector = new ConcurrentHashMap <String,Double>();
		this.documentCosines = new ConcurrentHashMap<String, DocumentTerm>();
	}
	
	//purpose: for each query term, calculate tf-idf
	//paramters: none
	//returns: none
	public void calculateAndSetQueryVector(){
		//calculate query keyword frequencies and store in vector
		ConcurrentHashMap <String,Double> queryVector = new ConcurrentHashMap <String, Double>();
		ListIterator<Keyword> queryIterator = queryTerms.listIterator();
		while(queryIterator.hasNext()){
			Keyword keyword = queryIterator.next();
			//for first query keyword appearence, add to vecotr and set frequency to 1 
			if(queryVector.get(keyword.getKeyword()) == null){
				queryVector.put(keyword.getKeyword(), 1.0);
			}
			//for subsequent query keyword appearences, increment vector entry frequency by 1
			else
				queryVector.replace(keyword.getKeyword(), queryVector.get(keyword) + 1.0);
		}
		
		double vectorNorm = 0.0;
		//convert  keyword frequencies to tf-idf values in the query vector
		Iterator<String> vectorKeys = queryVector.keySet().iterator();
		//for each key 
		while(vectorKeys.hasNext()){
			String key = vectorKeys.next(); 
			//get qeury term frequency
			Double termWeight = queryVector.get(key);
			
			Keyword keyword = indicies.getDictionary().get(key);
			//if keyword isn't in dictionary...
			if(keyword == null)			
				//set term weight to 0
				termWeight = 0.0;
			//otherwise, set term wieght to tf-idf
			else termWeight = (1 + Math.log10(termWeight)/Math.log10(2))*keyword.getInverseDocumentFrequency();
			
			//add query term's tf-idf to vector
			queryVector.replace(key, termWeight);
			//add sqaure of term weight to vector norm
			vectorNorm += Math.pow(termWeight, 2);
		}
		
		this.vectorNorm = Math.sqrt(vectorNorm);
		this.queryVector = queryVector;
	}
			
	//purpose: For each document that contains one of the query terms, calculate cosine of vetors
	//parameters: none
	//returns: none
	public void calculateAndSetDocumentCosines(){
		
		//add all document names that contain the keywords to the cosine hashtable
		Iterator<String> vectorKeys = queryVector.keySet().iterator();
		//for each key 
		while(vectorKeys.hasNext()){
			String key = vectorKeys.next(); 
			Keyword keyword = this.indicies.getDictionary().get(key);
			//see if keyword is in the dictionary
			if(keyword == null)
				continue;
			//for each document that contains keyword, calculate cosine and add to hashtable
			Iterator<String> docKeys = keyword.getDocumentTerms().keySet().iterator();
			while(docKeys.hasNext()){
				String docKey = docKeys.next();
				//add document filename to cosine hashtable if not already there 
				if(!this.documentCosines.containsKey(docKey))
					documentCosines.put(docKey, new DocumentTerm(indicies.getDocumentVectors().get(docKey).getDocument(), 0.0));
			}
		}
		
		//calculate cosines for all documents in hashtable
		Iterator<String> cosineKeys = documentCosines.keySet().iterator();
		//for each Document... 
		while(cosineKeys.hasNext()){
			//get its document vector
			String docFileName = cosineKeys.next();
			DocumentVector dv = indicies.getDocumentVectors().get(docFileName);
			double dotProduct = 0.0;
			//calculate dot product between query vector and document vector
			vectorKeys = queryVector.keySet().iterator();
			while(vectorKeys.hasNext()){
				String key = vectorKeys.next(); 
				//see if document contains keyword
				DocumentTerm dt = dv.getDocumentTerms().get(key);
				//if it does, add the product of the term weights to the dot product variable
				if(dt !=null)
					dotProduct += queryVector.get(key)*dt.getDocumentTermWeight();
			}
			
			//Set the document weight
			this.documentCosines.get(docFileName).setDocumentTermWeight(dotProduct/(dv.getVectorNorm()*this.vectorNorm));
		}
	}
	
	// getters and setters
	public double getVectorNorm(){
		return this.vectorNorm;
	}
	
	public ConcurrentHashMap<String, DocumentTerm> getDocumentTerms() {
		return documentCosines;
	} 
	
	public String getKeyword(){
		return this.queryTerms.toString();
	}
}
