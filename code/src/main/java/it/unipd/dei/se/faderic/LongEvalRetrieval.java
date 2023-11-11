package it.unipd.dei.se.faderic;

import it.unipd.dei.se.faderic.index.DirectoryIndexer;
import it.unipd.dei.se.faderic.parse.LongEvalDocumentParser;
import it.unipd.dei.se.faderic.search.Searcher;
import it.unipd.dei.se.faderic.utils.ConfigProperties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.Similarity;

/**
 * Main class for the IR system using
 * <a href="https://lucene.apache.org/" target="_blank">ApacheLucene</a>
 * for the
 * <a href="https://clef-longeval.github.io/" target="_blank">LongEval-Retrieval</a>
 * task.
 */
public class LongEvalRetrieval {
    
    /**
     * Main method of the class.
     * Please use the {@code config.xml} file available in the {@code resources}
     * to set the desired configuration.
     *
     * @param args command line parameters that are not used
     * @throws Exception if something goes wrong while indexing and searching.
     */
    public static void main(String[] args) throws Exception {

        final int ramBuffer = 256;
        final String docsPath = ConfigProperties.getDocsPath();
        final String indexPath = ConfigProperties.getIndexPath();
        final String extension = "txt";
        final String charsetName = "UTF-8";
        final int expectedDocs = 1570734;

        final Analyzer a = ConfigProperties.getAnalyzer();

        final Similarity sim = ConfigProperties.getSimilarity();

        final String runPath = "experiment";
        final String runID = ConfigProperties.getRunID();
        final int maxDocsRetrieved = 1000;

        final String topicsFile = ConfigProperties.getTopicsFile();
        final int expectedTopics = 672;

        // indexing
        final DirectoryIndexer i = new DirectoryIndexer(a, sim, ramBuffer, indexPath, docsPath, extension,
                charsetName, expectedDocs, LongEvalDocumentParser.class);
        i.index();

        // searching
        final Searcher s = new Searcher(sim, indexPath, topicsFile, expectedTopics, runID, runPath, maxDocsRetrieved);
        s.search();
    }
}
