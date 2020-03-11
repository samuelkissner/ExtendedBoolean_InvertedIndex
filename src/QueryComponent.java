import java.util.concurrent.ConcurrentHashMap;

public interface QueryComponent {

	public ConcurrentHashMap<String, DocumentTerm> getDocumentTerms();
	public String getKeyword();
}
