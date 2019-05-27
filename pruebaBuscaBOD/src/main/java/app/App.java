package app;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class App {

	
    public static void main(String[] args)
    {
        
        String indexPath = "indexes";
        String docsPath = "pdfs";
        String[] toFind = {"recibidas", "UNIDO"};
        String path = "pdfs\\informe-brechas-2019-03.pdf";
        
        
        //Indexer indexer = new Indexer(indexPath, docsPath);
        //indexer.doIndex();
        IndexPDFFiles.main(args);
        Searcher searcher = new Searcher(indexPath, docsPath);
        try {
        	for (String word: toFind) {
			searcher.search("", word);
			
        	}
		} catch (IOException | ParseException | InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        try {
        	High h = new High();
        	h.highlight( path, toFind );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }

}
