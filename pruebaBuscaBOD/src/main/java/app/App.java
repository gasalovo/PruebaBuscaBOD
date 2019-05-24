package app;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

public class App {

	
    public static void main(String[] args)
    {
        
        String indexPath = "C:\\BuscaBOD\\indexes";
        String docsPath = "C:\\BuscaBOD\\pdfs";
        
        //Indexer indexer = new Indexer(indexPath, docsPath);
        //indexer.doIndex();
        IndexPDFFiles.main(args);
        Searcher searcher = new Searcher(indexPath, docsPath);
        try {
			searcher.search("", "Angular");
			searcher.search("", "Console");
			searcher.search("", "Amigo");
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
