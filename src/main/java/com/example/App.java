package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.Gson;

/**
 * @author Vito Proscia
 *         Example of indexing and searching documents with Apache Lucene
 *         (https://lucene.apache.org/)
 */

public class App {
    public static void main(String[] args) {

        try {

            // Setup index writer
            IndexWriter myIndexWriter = setIndexWriter("src/main/java/com/example/index/myIndex");
            // Reach map from json file
            Map<?, ?> users = readGson("./resources/profiles.json");
            // Write element of map in documents
            for (Object user : users.keySet()) {
                setDocument(myIndexWriter, ((Map<?, ?>) users.get(user)).get("Name").toString(),
                        ((Map<?, ?>) users.get(user)).get("Surname").toString(),
                        ((Map<?, ?>) users.get(user)).get("Address").toString());
            }

            // Close IndexWriter
            myIndexWriter.close();
            // Show search from query result
            System.out.println("Find " + makeQuery(myIndexWriter.getDirectory()) + " document(s)");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Set properties of index writer
     * 
     * @param path : path of Index directory
     * @return index writer on `path` directory
     * @throws IOException
     */
    public static IndexWriter setIndexWriter(String path) throws IOException {

        // Open the directory where the index is/will be saved
        FSDirectory myFsdir = FSDirectory.open(new File(path).toPath());

        // Creation configuration obj for index writer
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // Creation of indexWriter
        IndexWriter indexWriter = new IndexWriter(myFsdir, iwc);

        return indexWriter;
    }

    /**
     * Create a document with its field and add the document to the index
     * 
     * @param indexWriter
     * @param name        : field of doc
     * @param surname     : field of doc
     * @param address     : field of doc
     * @throws IOException
     */

    public static void setDocument(IndexWriter indexWriter, String name, String surname, String address)
            throws IOException {

        // Creation of document and add fields
        Document doc = new Document();
        doc.add(new TextField("Name", name, Field.Store.NO));
        doc.add(new TextField("Surname", surname, Field.Store.NO));
        doc.add(new TextField("Address", address, Field.Store.NO));
        // Add document to index
        indexWriter.addDocument(doc);

    }

    /**
     * Search for documents filtered by query
     * 
     * @param directory : dir of index
     * @return Number of document find
     * @throws IOException
     * @throws ParseException
     */

    public static long makeQuery(Directory directory) throws IOException, ParseException {

        // Creation od the IndexSearcher
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
        // Creation of the query parser with the default field and analyzer
        QueryParser queryParser = new QueryParser("Name", new StandardAnalyzer());
        // Parse the query
        Query q = queryParser.parse("Stefano");
        // Search
        TopDocs topDocs = indexSearcher.search(q, 10);

        return topDocs.totalHits.value;
    }

    /**
     * Read element from json file and convert them in map
     * 
     * @param path : path of json file to read
     * @return map of obj in json file
     * @throws IOException
     */

    public static Map<?, ?> readGson(String path) throws IOException {

        // Creation of reader form file
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        Gson gson = new Gson();
        // Convert json file to Map
        Map<?, ?> users = gson.fromJson(reader, Map.class);

        reader.close();
        return users;

    }

}