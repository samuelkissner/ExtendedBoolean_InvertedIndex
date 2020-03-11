import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Indicies{
	
	private File[] files;
	//inverted index
	private ConcurrentHashMap<String, Keyword> dictionary;
	//foreward index
	private ConcurrentHashMap<String, DocumentVector> documentVectors;
	
	public Indicies(File documentFolder){
		this.files = documentFolder.listFiles();
		this.dictionary = new ConcurrentHashMap<String, Keyword>();
		this.documentVectors = new ConcurrentHashMap<String, DocumentVector>();
	}
	
	//purpose: build dictionary and document vector using terms found in "files" array
	//parameters: none
	//returns: none
	public void buildIndicies() {
		for(int i = 0; i<files.length;i++){
			try { 
				
				Scanner input = new Scanner(files[i]);
				String line;
				Integer tokenLocation = 0;
		        
				//add document to forewrad index
				documentVectors.put(files[i].getName(),new DocumentVector(files[i]));

				while(input.hasNextLine()){	
					line = input.nextLine();
						
		
					//abstract processing
					if(line.contains("<ABSTRACT>")){
						while(!line.contains("</ABSTRACT>")){
							
							String[] tokens = line.split(" ");
							//process this line of terms
							tokenLocation = processTokens(tokens, tokenLocation, files[i]);
							if(input.hasNextLine())
								line = input.nextLine();
						}							
						String[] tokens = line.split(" ");
						//process this final line of terms
						tokenLocation = processTokens(tokens, tokenLocation, files[i]);
					}
				}
				input.close();
			
			
			
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		calculateAndSetDocumentTermWeights();
		//calculate document vector norms
		Iterator<String> dvKeys = documentVectors.keySet().iterator();
		while(dvKeys.hasNext()){
			documentVectors.get(dvKeys.next()).calculateAndSetVectorNorm();
		}
	}
	

	//purpose: Process words in string array and add to dictionary if they meet criteria. 
	//		   Also, add associated DocumentTerms to dictionary and document vector	
	//paramters: String[] tokens, int location, File docFile
	//returns: int location
	public int processTokens(String[] tokens, Integer tokenLocation, File docFile){
		
		for(String token : tokens){
			tokenLocation++;
			
			//porter's stemming
			token = Stemmer.pstem(token);
			
			if(token.length()>=4){
				
				Keyword keyword = dictionary.get(token);
				//if keyword not in dictionary, add it to the dictionary
				if(keyword == null){
					
					keyword = new Keyword(token);
					//create new document-term instance
					DocumentTerm docTerm = new DocumentTerm(docFile, token);
					//add location of term in document
					docTerm.getTermLocations().put(tokenLocation, true);
					//add documentTerm to term's list of document
					keyword.getDocumentTerms().put(docTerm.getDocument().getName(),docTerm);
					//add keyword to dictionary
					dictionary.put(token, keyword);
					//NEW FOR EXTENDED BOOL
					//also add DocumentTerm instance to the documentVector
					documentVectors.get(docFile.getName()).getDocumentTerms().put(token, docTerm);
				}
				// if keyword is in dictionary, update entry and add document information
				else{
					//increment term count
					keyword.increaseCountByOne();
					//check to see if document is already in the term's document list, create new document if not 
					DocumentTerm docTerm = keyword.getDocumentTerms().get(docFile.getName());
					if(docTerm == null){
						docTerm = new DocumentTerm(docFile, keyword.getKeyword());
						//add new documentTerm to keyword's list
						keyword.getDocumentTerms().put(docTerm.getDocument().getName(),docTerm);
						//NEW FOR EXTENDED BOOL
						//also add DocumentTerm instance to the documentVector
						documentVectors.get(docFile.getName()).getDocumentTerms().put(token, docTerm);
					}
					//add location to document-term instance
					docTerm.getTermLocations().put(tokenLocation, true);
				}
			}
		}
		return tokenLocation;
	}
	
	
	//purpose: calculate and set TF-IDFs for each keyword's document term instances.
	// 		   Also, remove keywords referenced by only one document	
	//paramters: none
	//returns: none
	public void calculateAndSetDocumentTermWeights(){
		
		Iterator<String> keys = dictionary.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next(); 
			Keyword keyword = dictionary.get(key);
			if(keyword.getDocumentTerms().size() <= 1)
				dictionary.remove(key);
			
			//calculate TF-IDF
			else{
				keyword.calculateAndSetInverseDocumentFrequency(files.length);
				keyword.calculateAndSetAllDocumentTermWeights();
			}
		}
	}			

	
	//purpose: return a copy of the document list for a specific keyword
	//paraters: String keyword
	//returns: ConcurrentHashMap<String, DocumentTerm> documents
	public ConcurrentHashMap<String, DocumentTerm> getKeywordTermDocumentList(String term){
		//if term is in dictionary, retrieve a copy of its document list
		Keyword keyword = dictionary.get(term);
		if(keyword != null)
			//return a copy of the array list
			return new ConcurrentHashMap<String, DocumentTerm>(keyword.getDocumentTerms());
			
		return null;
	}
	
	
	//purpose: print inverted index
	public void printDictionary(){
		
		Iterator<String> keys = dictionary.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next(); 
			Keyword keyword = dictionary.get(key);
			System.out.print(keyword.getKeyword()  + " " + keyword.getCount()+" -> ");
			Iterator<String> docKeys = keyword.getDocumentTerms().keySet().iterator();
			while(docKeys.hasNext()){
				String docKey = docKeys.next();
				DocumentTerm document = keyword.getDocumentTerms().get(docKey);
				System.out.print("[" + document.getDocument().getName() +", tf-idf= " + document.getDocumentTermWeight() + " (");
				
				Iterator<Integer> locations =  document.getTermLocations().keySet().iterator();
				while(locations.hasNext()){
					Integer location = locations.next();
					System.out.print(location + ", ");
				}
				System.out.print(")] ");
			}
			System.out.println();
		}
	}
	
	public ConcurrentHashMap<String, Keyword> getDictionary() {
		return dictionary;
	}

	public void setDictionary(ConcurrentHashMap<String, Keyword> dictionary) {
		this.dictionary = dictionary;
	}
	
	public ConcurrentHashMap<String, DocumentVector> getDocumentVectors(){
		return this.documentVectors;
	}

	//getters and setters
	public File[] getFiles() {
		return files;
	}
}
