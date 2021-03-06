package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Index all pdf files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run it with no command-line arguments for
 * usage information.
 * <p>
 * It's based on a demo provided by the lucene project.
 */
public final class IndexPDFFiles
{

    private IndexPDFFiles()
    {
    }

    /**
     * Index all text files under a directory.
     * 
     * @param args command line arguments
     * 
     */
    public static void main(String[] args)
    {
    	//path donde va la base de datos
    	String indexPath = "indexes";
    	
    	//path donde van los pdf
        String docsPath = "pdfs";
        
        //true = crear nueva base de datos, false = update base datos existente
        boolean create = true;

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead())
        {
            System.out.println("Document directory '" + docDir.getAbsolutePath()
                    + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try
        {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            try (Directory dir = FSDirectory.open(new File(indexPath).toPath()))
            {
                Analyzer analyzer = new StandardAnalyzer();
                IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
                if (create)
                {
                    // Create a new index in the directory, removing any
                    // previously indexed documents:
                    iwc.setOpenMode(OpenMode.CREATE);
                }
                else
                {
                    // Add new documents to an existing index:
                    iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
                }
                // Optional: for better indexing performance, if you
                // are indexing many documents, increase the RAM
                // buffer. But if you do this, increase the max heap
                // size to the JVM (eg add -Xmx512m or -Xmx1g):
                //
                // iwc.setRAMBufferSizeMB(256.0);
                try (final IndexWriter writer = new IndexWriter(dir, iwc))
                {
                    indexDocs(writer, docDir);
                    
                    // NOTE: if you want to maximize search performance,
                    // you can optionally call forceMerge here. This can be
                    // a terribly costly operation, so generally it's only
                    // worth it when your index is relatively static (ie
                    // you're done adding documents to it):
                    //
                    // writer.forceMerge(1);
                }
            }

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        }
        catch (IOException e)
        {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given, recurses over files and directories
     * found under the given directory.
     * 
     * NOTE: This method indexes one document per input file. This is slow. For good throughput, put multiple documents
     * into your input file(s). An example of this is in the benchmark module, which can create "line doc" files, one
     * document per line, using the <a
     * href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     * 
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param file The file to index, or the directory to recurse into to find files to index
     * @throws IOException If there is a low-level I/O error
     */
    static void indexDocs(IndexWriter writer, File file) throws IOException
    {
        // do not try to index files that cannot be read
        if (file.canRead())
        {
            if (file.isDirectory())
            {
                String[] files = file.list();
                // an IO error could occur
                if (files != null)
                {
                    for (String fileName : files)
                    {
                        indexDocs(writer, new File(file, fileName));
                    }
                }
            }
            else
            {

                FileInputStream fis;
                try
                {
                    fis = new FileInputStream(file);
                }
                catch (FileNotFoundException fnfe)
                {
                    // at least on windows, some temporary files raise this exception with an "access denied" message
                    // checking if the file can be read doesn't help
                    return;
                }

                try
                {

                    String path = file.getName().toUpperCase();
                    Document doc;
                    if (path.toLowerCase().endsWith(".pdf"))
                    {
                        System.out.println("Indexing PDF document: " + file);
                        doc = LucenePDFDocument.getDocument(file);
                    }
                    else
                    {
                        System.out.println("Skipping " + file);
                        return;
                    }

                    if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
                    {
                        // New index, so we just add the document (no old document can be there):
                        System.out.println("adding " + file);
                        writer.addDocument(doc);
                    }
                    else
                    {
                        // Existing index (an old copy of this document may have been indexed) so
                        // we use updateDocument instead to replace the old one matching the exact
                        // path, if present:
                        System.out.println("updating " + file);
                        writer.updateDocument(new Term("uid", LucenePDFDocument.createUID(file)), doc);
                    }
                }
                finally
                {
                    fis.close();
                }
            }
        }
    }
}