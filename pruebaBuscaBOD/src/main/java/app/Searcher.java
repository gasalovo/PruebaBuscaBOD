package app;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hamcrest.SelfDescribing;

public class Searcher {
	
	String indexPath = null;
    String docsPath = null;
    Query query;
    
	public Searcher(String indexPath, String docsPath) {
		this.indexPath = indexPath;
		this.docsPath = docsPath;
	}
	
	public void search(String field, String toFind) throws IOException, ParseException, InvalidTokenOffsetsException{
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
        
      //Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter();
        
      //analyzer with the default stop words
        Analyzer analyzer = new StandardAnalyzer();
        
        //Get directory reference
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        
      //Query parser to be used for creating TermQuery
        //QueryParser qp = new QueryParser("All", analyzer);
         
        //Create the query
        //Query query = qp.parse(toFind);
        
        //It scores text fragments by the number of unique query terms found
        //Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);
         
        //used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);
         
        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);
         
        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
        //Fragmenter fragmenter = new SimpleFragmenter(10);
         
        //set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);
         
        //Iterate over found results
        for (int i = 0; i < foundDocs.scoreDocs.length; i++) 
        {
            int docid = foundDocs.scoreDocs[i].doc;
            Document doc = searcher.doc(docid);
            String title = doc.get("path");
             
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + title);
             
            //Get stored text from found document
            String text = doc.get("contents");
 
            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);
             
            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, 10);
            for (String frag : frags) 
            {
                System.out.println("=======================");
                System.out.println(frag);
            }
        
        }
        
	}
	
	private TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws IOException, ParseException
    {
        //Create search query
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
		Query query = qp.parse(textToFind);
		this.query = query; //keep to highlighter
		
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
