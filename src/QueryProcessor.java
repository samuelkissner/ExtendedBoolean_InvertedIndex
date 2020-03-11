import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueryProcessor {

	private Indicies indicies;
	HashMap<String, Integer> operators;
	
	public QueryProcessor(Indicies indicies){
		this.indicies = indicies;
		this.operators = new HashMap<String, Integer>();
		operators.put("or",0);
		operators.put("and",1);
	}
	
	//purpose: process a query
	//parameters: String query
	//returns: ArrayList<DocumentTerm>
	public ArrayList<DocumentTerm> processQuery(String query){
		
		String[] infixExpression = query.split(" ");
		
		//convert from infix to postfix
		ArrayList<QueryComponent> postfixExpression = infixToPostfix(infixExpression);
		ConcurrentHashMap<String, DocumentTerm> resultMap = evaluatePostfix(postfixExpression);
		
		ArrayList<DocumentTerm> resultList = new ArrayList<DocumentTerm>(resultMap.values());
		return resultList;
		
	}
	
	//MODIFIED FOR EXTENDED BOOL
	//purpose: convert expression from infix to postfix
	//paramters: String[] infixExpression
	//retruns: ArrayList<QueryComponent> postfixExpression
	public ArrayList<QueryComponent> infixToPostfix(String[] infixEx){
		
		
		//isolate sequence of query terms
		ArrayList<QueryComponent> infixExpressionKeywords = new ArrayList<QueryComponent>();
		for(int i = 0;i<infixEx.length;i++){
			if(operators.get(infixEx[i]) !=null)
				infixExpressionKeywords.add(new Keyword(infixEx[i]));
				//if it's not an operator..
			else{
				//if the next token is an operator 
				if(i == infixEx.length - 1 || operators.get(infixEx[i + 1]) !=null){
					Keyword keyword = new Keyword(Stemmer.pstem(infixEx[i]));
					//retrieve documentSet for this keyword
					ConcurrentHashMap<String, DocumentTerm> documentSet = indicies.getKeywordTermDocumentList((keyword.getKeyword()));
					if(documentSet != null)
						//add documentSet to keyword
						keyword.setDocumentTerms(documentSet);
					infixExpressionKeywords.add(keyword);
				}else{
					//multiple keywords in a row - find cosines of weights
					ArrayList<Keyword> queryTerms = new ArrayList<Keyword>();
					int j=i;
					do{
						Keyword keyword = new Keyword(Stemmer.pstem(infixEx[j]));
						//retrieve documentSet for this keyword
						ConcurrentHashMap<String, DocumentTerm> documentSet = indicies.getKeywordTermDocumentList((keyword.getKeyword()));
						//if not null
						if(documentSet != null)
							//add documentSet to keyword
							keyword.setDocumentTerms(documentSet);
						queryTerms.add(keyword);
						j++;
					}while(j<infixEx.length &&operators.get(infixEx[j]) ==null);
						
					//calculate vector cosines between documents and query
					infixExpressionKeywords.add(processQueryTerms(queryTerms));
					i=j-1;
				}
			}
				
		}
		
		//convert infix to iterator
		ListIterator<QueryComponent> infix = infixExpressionKeywords.listIterator();
		//create array list for postfix
		ArrayList<QueryComponent> postfixExpression = new ArrayList<QueryComponent>();
		//create stack for operators
		Stack<QueryComponent> stack = new Stack<QueryComponent>(); 
		
		//convert infix to postfix
		while(infix.hasNext()){
			QueryComponent queryComponent = infix.next();
			String token = queryComponent.getKeyword();
			
			
			//if it's an operator
			if(operators.get(token) != null){
				//add to stack
				stack.push(queryComponent);
				
			}else{
				//add to postfix expression
				postfixExpression.add(queryComponent); 
			}		
		}
		
		//pop operator off the stack and add to expression
		if(!stack.isEmpty())
			postfixExpression.add(stack.pop());
			
		return postfixExpression; 
	}
	
	//purpose: evaluate postfix expression
	//paramters: ArrayList<QueryComponenet> postfixExpression
	//retruns: ConcurrentHashMap<String, DocumentTerm> resultSet
	public ConcurrentHashMap<String, DocumentTerm>  evaluatePostfix(ArrayList<QueryComponent> postfixExpression){
		//ArrayList for resulting document set
		ConcurrentHashMap<String, DocumentTerm> resultSet = new ConcurrentHashMap<String, DocumentTerm>();
		//iterator for the expression
		ListIterator<QueryComponent> postEx = postfixExpression.listIterator();
		//stack
		Stack<ConcurrentHashMap<String, DocumentTerm>> stack = new Stack<ConcurrentHashMap<String, DocumentTerm>>();
		
		while(postEx.hasNext()){
			QueryComponent queryComponent = postEx.next();
			String token = queryComponent.getKeyword();
			
			//if not an operator 
			if(operators.get(token) == null){
				stack.push(queryComponent.getDocumentTerms());
			
				//if it is an operator...
			}else{
				
				//and operation	
				if(token.equals("and")){
					//pop first two document sets off stack 
					ConcurrentHashMap<String, DocumentTerm> dS2 = stack.pop();
					ConcurrentHashMap<String, DocumentTerm> dS1 = stack.pop();
					//evaluate
					return andOperation(dS1,dS2);
		
				
				//or operation
				}else if(token.equals("or")){
					//pop first two document sets off stack 
					ConcurrentHashMap<String, DocumentTerm> dS2 = stack.pop();
					ConcurrentHashMap<String, DocumentTerm> dS1 = stack.pop();
					//evaluate
					return orOperation(dS1,dS2);
			
				}
			}
		}

		resultSet = stack.pop();
		return resultSet;
	}
	
	//OPERATIONS
	
	
	//purpose: perform an and operation
	//parameters: ConcurrentHashMap<String, DocumentTerm> docSet1, ConcurrentHashMap<String, DocumentTerm> docSet2
	//returns: ConcurrentHashMap<String, DocumentTerm> resultSet
	public ConcurrentHashMap<String, DocumentTerm> andOperation(ConcurrentHashMap<String, DocumentTerm> docSet1, ConcurrentHashMap<String, DocumentTerm> docSet2){
		
		//look through both sets of documents for max term weight. This will be used to normalize term weights
		double maxTermWeight = 0.0;
		Iterator<String> docFileNames = docSet1.keySet().iterator();
		while(docFileNames.hasNext()){
			double w = docSet1.get(docFileNames.next()).getDocumentTermWeight();
			if(w>maxTermWeight)
				maxTermWeight = w;
		}
		
		docFileNames = docSet2.keySet().iterator();
		while(docFileNames.hasNext()){
			double w = docSet2.get(docFileNames.next() ).getDocumentTermWeight();
			if(w>maxTermWeight)
				maxTermWeight = w;
		}
		
		
		ConcurrentHashMap<String, DocumentTerm> resultSet = new ConcurrentHashMap<String, DocumentTerm>();
		
		docFileNames = docSet1.keySet().iterator();
		//compare all entries in docSet1 to docSet2
		while(docFileNames.hasNext()){
			String key = docFileNames.next(); 
			double w1 = docSet1.get(key).getDocumentTermWeight();
			double w2 = 0.0;
			DocumentTerm dt2;
			//remove shared documents between two sets
			if((dt2 = docSet2.remove(key))!=null)
				w2 = dt2.getDocumentTermWeight();
			
			double sim = 1 - Math.sqrt((Math.pow(1-w1/maxTermWeight, 2) + Math.pow(1-w2/maxTermWeight, 2))/2);
			resultSet.put(key, new DocumentTerm(this.indicies.getDocumentVectors().get(key).getDocument(), sim));
		}
		
		docFileNames = docSet2.keySet().iterator();
		//calculate sim score for any remaining documents in set 2
		while(docFileNames.hasNext()){
			String key = docFileNames.next(); 
			double w1 = docSet2.get(key).getDocumentTermWeight();	
			double sim = 1 - Math.sqrt((Math.pow(1-w1/maxTermWeight, 2) + 1)/2);
			resultSet.put(key, new DocumentTerm(this.indicies.getDocumentVectors().get(key).getDocument(), sim));
		}
		
		return resultSet;
	}
	//purpose: perform an or operation
	//parameters: ConcurrentHashMap<String, DocumentTerm> docSet1, ConcurrentHashMap<String, DocumentTerm> docSet2
	//returns: ConcurrentHashMap<String, DocumentTerm> resultSet
	public ConcurrentHashMap<String, DocumentTerm> orOperation(ConcurrentHashMap<String, DocumentTerm> docSet1, ConcurrentHashMap<String, DocumentTerm> docSet2){
ConcurrentHashMap<String, DocumentTerm> resultSet = new ConcurrentHashMap<String, DocumentTerm>();
		
		
		Iterator<String> docFileNames = docSet1.keySet().iterator();
		
		//compare all entries in docSet1 to docSet2
		while(docFileNames.hasNext()){
			String key = docFileNames.next(); 
			double w1 = docSet1.get(key).getDocumentTermWeight();
			double w2 = 0.0;
			DocumentTerm dt2;
			//remove shared documents between two sets
			if((dt2 = docSet2.remove(key))!=null)
				w2 = dt2.getDocumentTermWeight();
			
			double sim = Math.sqrt(Math.pow(w1, 2) + Math.pow(w2, 2));
			resultSet.put(key, new DocumentTerm(this.indicies.getDocumentVectors().get(key).getDocument(), sim));
		}
		
		docFileNames = docSet2.keySet().iterator();
		//calculate sim score for any remaining documents in set 2
		while(docFileNames.hasNext()){
			String key = docFileNames.next(); 
			double w1 = docSet2.get(key).getDocumentTermWeight();	
			double sim = Math.sqrt(Math.pow(w1, 2));
			resultSet.put(key, new DocumentTerm(this.indicies.getDocumentVectors().get(key).getDocument(), sim));
		}
		
		return resultSet;
	}
	
	//NEW FOR EXTENDED BOOL
	//purpose: Create query instance, calculate vector cosine compared to query
	//parameters: ArrayList<Keyword> query
	//returns: Query query
	public MultiWordQuery processQueryTerms(ArrayList<Keyword> queryTerms){
		
		MultiWordQuery query = new MultiWordQuery(queryTerms, indicies);
		//calculate tf-idf for queryterms
		query.calculateAndSetQueryVector();
		//calculate cosine between query and each document that includes at leaset on of the query terms
		query.calculateAndSetDocumentCosines();
		return query;
	}
	
	//purpose: for doc set, print top 10 filenames and weights
	//paramters: ArrayList<DocumentTerm> docSet
	public String listDocSet(ArrayList<DocumentTerm> docSet){
		String list= "";
		ListIterator<DocumentTerm> docs = docSet.listIterator();
		int i = 0;
		while(docs.hasNext()){
			DocumentTerm docTerm = docs.next();
			list = list + docTerm.getDocument().getName() + " weight = " + docTerm.getDocumentTermWeight() +"\n";
			if(++i >=10 )
				break;
		}
		
		return list;
	}
}
