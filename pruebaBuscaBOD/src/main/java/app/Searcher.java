package app;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	
	String indexPath = null;
    String docsPath = null;
    
	public Searcher(String indexPath, String docsPath) {
		this.indexPath = indexPath;
		this.docsPath = docsPath;
	}
	
	public void search(String field, String toFind) throws IOException, ParseException{
		//Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        //Search indexed contents using search term
        TopDocs foundDocs = searchInContent(toFind, searcher);
         
        //Total found documents
        System.out.println("Total Results: " + foundDocs.totalHits);
         
        //Let's print out the path of files which have searched term
        for (ScoreDoc sd : foundDocs.scoreDocs) 
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("Path: "+ d.get("path") + ", Score: " + sd.score);
            //System.out.println("Contents: "+ d.get("contents"));
            //System.out.println("All: "+ d.get("All"));
            //System.out.println("Summary: "+ d.get("summary"));
            System.out.println("Pages: "+ d.get("Pages"));
        }
	}
	
	private TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws IOException, ParseException
    {
        //Create search query
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
		Query query = qp.parse(textToFind);
		
        //search the index
        TopDocs hits = searcher.search(query, 10);
        return hits;
    }
 
    private IndexSearcher createSearcher() throws IOException 
    {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
}
