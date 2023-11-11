package it.unipd.dei.se.faderic.search;

import it.unipd.dei.se.faderic.analyze.ShingleAnalyzer;
import it.unipd.dei.se.faderic.parse.ParsedDocument;
import it.unipd.dei.se.faderic.utils.ConfigProperties;
import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import jep.JepException;
import jep.SharedInterpreter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Searches a document collection.
 */
public class Searcher {

    /**
     * The fields of the typical TREC topics.
     */
    private static final class TOPIC_FIELDS {

        /**
         * The title of a topic.
         */
        public static final String TITLE = "title";
    }

    /**
     * The identifier of the run
     */
    private final String runID;

    /**
     * The run to be written
     */
    private final PrintWriter run;

    /**
     * The index reader
     */
    private final IndexReader reader;

    /**
     * The index searcher.
     */
    private final IndexSearcher searcher;

    /**
     * The topics to be searched
     */
    private final QualityQuery[] topics;

    /**
     * The query parser
     */
    private final QueryParser qp;

    /**
     * The maximum number of documents to retrieve
     */
    private final int maxDocsRetrieved;

    /**
     * The total elapsed time.
     */
    private long elapsedTime = Long.MIN_VALUE;

    /**
     * The spell checker
     */
    private SpellChecker spellChecker = null;

    /**
     * The Python interpreter for the reranking
     */
    private SharedInterpreter pythonInterpreter = null;

    /**
     * Creates a new searcher.
     *
     * @param similarity       the {@code Similarity} to be used.
     * @param indexPath        the directory where containing the index to be
     *                         searched.
     * @param topicsFile       the file containing the topics to search for.
     * @param expectedTopics   the total number of topics expected to be searched.
     * @param runID            the identifier of the run to be created.
     * @param runPath          the path where to store the run.
     * @param maxDocsRetrieved the maximum number of documents to be retrieved.
     * @throws NullPointerException     if any of the parameters is {@code null}.
     * @throws IllegalArgumentException if any of the parameters assumes invalid
     *                                  values.
     */
    public Searcher(final Similarity similarity, final String indexPath,
            final String topicsFile, final int expectedTopics, final String runID, final String runPath,
            final int maxDocsRetrieved) {

        if (similarity == null) {
            throw new NullPointerException("Similarity cannot be null.");
        }

        if (indexPath == null) {
            throw new NullPointerException("Index path cannot be null.");
        }

        if (indexPath.isEmpty()) {
            throw new IllegalArgumentException("Index path cannot be empty.");
        }

        final Path indexDir = Paths.get(indexPath);
        if (!Files.isReadable(indexDir)) {
            throw new IllegalArgumentException(
                    String.format("Index directory %s cannot be read.", indexDir.toAbsolutePath().toString()));
        }

        if (!Files.isDirectory(indexDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to search the index.",
                    indexDir.toAbsolutePath().toString()));
        }

        try {
            reader = DirectoryReader.open(FSDirectory.open(indexDir));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to create the index reader for directory %s: %s.",
                    indexDir.toAbsolutePath().toString(), e.getMessage()), e);
        }

        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        if (topicsFile == null) {
            throw new NullPointerException("Topics file cannot be null.");
        }

        if (topicsFile.isEmpty()) {
            throw new IllegalArgumentException("Topics file cannot be empty.");
        }

        try {
            BufferedReader in = Files.newBufferedReader(Paths.get(topicsFile), StandardCharsets.UTF_8);

            topics = new LongEvalTopicsReader().readQueries(in);

            in.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to process topic file %s: %s.", topicsFile, e.getMessage()), e);
        }

        if (expectedTopics <= 0) {
            throw new IllegalArgumentException(
                    "The expected number of topics to be searched cannot be less than or equal to zero.");
        }

        if (topics.length != expectedTopics) {
            System.out.printf("Expected to search for %s topics; %s topics found instead.", expectedTopics,
                    topics.length);
        }

        qp = new QueryParser(ParsedDocument.FIELDS.BODY, new WhitespaceAnalyzer());

        if (runID == null) {
            throw new NullPointerException("Run identifier cannot be null.");
        }

        if (runID.isEmpty()) {
            throw new IllegalArgumentException("Run identifier cannot be empty.");
        }

        this.runID = runID;

        if (runPath == null) {
            throw new NullPointerException("Run path cannot be null.");
        }

        if (runPath.isEmpty()) {
            throw new IllegalArgumentException("Run path cannot be empty.");
        }

        Path runDir = Paths.get(runPath);
        if (!Files.isWritable(runDir)) {
            throw new IllegalArgumentException(
                    String.format("Run directory %s cannot be written.", runDir.toAbsolutePath().toString()));
        }

        if (!Files.isDirectory(runDir)) {
            throw new IllegalArgumentException(String.format("%s expected to be a directory where to write the run.",
                    runDir.toAbsolutePath().toString()));
        }

        Path runFile = runDir.resolve(runID + ".txt");
        try {
            run = new PrintWriter(Files.newBufferedWriter(runFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to open run file %s: %s.", runFile.toAbsolutePath(), e.getMessage()), e);
        }

        if (maxDocsRetrieved <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of documents to be retrieved cannot be less than or equal to zero.");
        }

        this.maxDocsRetrieved = maxDocsRetrieved;

        PlainTextDictionary dict = ConfigProperties.getSpellCheckDict();
        if (dict != null) {
            try {
                File dir = new File("experiment/spellchecker/");
                Directory directory = FSDirectory.open(dir.toPath());
                this.spellChecker = new SpellChecker(directory);
                this.spellChecker.indexDictionary(dict, new IndexWriterConfig(), true);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Exception occurred while initializing SpellChecker", e);
            }
        }

        if (ConfigProperties.useReranking()) {
            try {
                System.out.println("Loading the reranker model... (It might take some time the first time)");
                pythonInterpreter = new SharedInterpreter();
                pythonInterpreter.runScript("src/main/python/bert_reranker.py");
                Object result = pythonInterpreter.invoke("load_model", ConfigProperties.getRerankBERTModel());
                if (! (Boolean) result)
                    throw new IllegalStateException("The given rerank model is not a BERT model or is not available.");
                else
                    System.out.println("The model has been correctly loaded.");
            } catch (UnsatisfiedLinkError | JepException e) {
                throw new IllegalStateException(
                        "To use reranking please follow the instructions available in the project README.md", e);
            }
        }
    }

    /**
     * Returns the total elapsed time.
     *
     * @return the total elapsed time.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * The function reranks a set of documents based on a given query and updates their scores
     * accordingly.
     * 
     * @param t A {@code QualityQuery} object representing the query to be reranked.
     * @param sd An array of {@code ScoreDoc} objects representing the top documents retrieved by the initial
     * search query.
     * @param numDocsToRerank The number of documents to be reranked.
     * @param rerankScoreWeight The weight given to the reranked score in the final ranking of the
     * documents. It is a {@code float} value between {@code 0} and {@code 1}.
     */
    private void rerank(QualityQuery t, ScoreDoc[] sd, int numDocsToRerank, float rerankScoreWeight) throws IOException {
        // numDocsToRerank has to be adjusted if the number of hits is smaller
        numDocsToRerank = Math.min(numDocsToRerank, sd.length);

        // Create input for the reranker
        ArrayList<String> docID = new ArrayList<String>(), docBody = new ArrayList<String>();
        for (int i = 0; i < numDocsToRerank; i++) {
            docID.add(reader.storedFields().document(sd[i].doc).get(ParsedDocument.FIELDS.ID));
            docBody.add(reader.storedFields().document(sd[i].doc).get(ParsedDocument.FIELDS.BODY));
        }

        // Invoke the reranker
        Object result = pythonInterpreter.invoke("rerank", t.getValue(TOPIC_FIELDS.TITLE), docID, docBody);

        // Convert the returned object to a float ArrayList
        ArrayList<Float> rerankScores = ((ArrayList<?>) result)
                .stream()
                .map(object -> ((Double) object).floatValue())
                .collect(Collectors.toCollection(ArrayList::new));

        // Normalize the rerank scores with respect to the original ones
        float minRerankedScore = Float.MAX_VALUE;
        float maxRerankedScore = Float.MIN_VALUE;
        for (int i = 0; i < rerankScores.size(); i++) {
            float score = rerankScores.get(i);
            if (score > maxRerankedScore)
                maxRerankedScore = score;
            if (score < minRerankedScore)
                minRerankedScore = score;
        }
        minRerankedScore = Math.abs(minRerankedScore);  // Will be added to the others to make them positive

        maxRerankedScore += minRerankedScore;           // Will be used to normalize reranked scores
        float maxToRerankScore = sd[0].score;           // Will be used to normalize reranked score

        float maxNotToRerankScore;                      // Will be used to preserve other docs ranking
        if (numDocsToRerank < sd.length)
            maxNotToRerankScore = sd[numDocsToRerank].score + 0.000001f;
        else
            maxNotToRerankScore = 0;

        for (int i = 0; i < numDocsToRerank; i++) {
            float normalizedRerankScore = (rerankScores.get(i) + minRerankedScore)  // Make score positive
                    * maxToRerankScore                                              // Normalize score
                    / maxRerankedScore;
            sd[i].score = maxNotToRerankScore                                       // Preserve other docs ranking
                    + (1 - rerankScoreWeight) * sd[i].score                         // Weight original score
                    + rerankScoreWeight * normalizedRerankScore;                    // Weight rerank score
        }

        // Sort only the documents that have been reranked
        Arrays.sort(sd, 0, numDocsToRerank, new Comparator<ScoreDoc>() {
            @Override
            public int compare(ScoreDoc d1, ScoreDoc d2) {
                if (d1.score == d2.score)
                    return 0;
                return d1.score < d2.score ? 1 : -1;
            }
        });
    }

    /**
     * Searches for the specified topics.
     *
     * @throws IOException    if something goes wrong while searching.
     * @throws ParseException if something goes wrong while parsing topics.
     */
    public void search() throws IOException, ParseException {

        System.out.printf("%n#### Start searching ####%n");

        // the start time of the searching
        final long start = System.currentTimeMillis();

        final Set<String> idField = new HashSet<>();
        idField.add(ParsedDocument.FIELDS.ID);

        BooleanQuery.Builder bq = null;
        Query q = null;
        String queryStr = null;

        TopDocs docs = null;
        ScoreDoc[] sd = null;
        String docID = null;
        int nTopics = topics.length;
        int topicsCount = 0;

        Analyzer stdAnalyzer = ConfigProperties.getAnalyzer();

        float boost = 1;
        int fuzzyThreshold = ConfigProperties.getFuzzyThreshold();

        int numSuggestions = 5;

        Analyzer synAnalyzer = ConfigProperties.getSynonymAnalyzer();

        int shingleProximity = 5;
        int maxShingleSize = ConfigProperties.getMaxShingleSize();
        Analyzer shingleAnalyzer = new ShingleAnalyzer(maxShingleSize);

        int numDocsToRerank = ConfigProperties.getNumDocsToRerank();
        float rerankScoreWeight = ConfigProperties.getRerankScoreWeight();

        try {
            for (QualityQuery t : topics) {
                topicsCount++;

                System.out.printf("Searching for topic %d of %d: id=%s,title=%s%n",
                        topicsCount, nTopics, t.getQueryID(), t.getValue(TOPIC_FIELDS.TITLE));

                bq = new BooleanQuery.Builder();

                String title = QueryParserBase.escape(t.getValue(TOPIC_FIELDS.TITLE));
                List<String> titleWords = tokenizeString(stdAnalyzer, title);
                String analyzedTitle = String.join(" ", titleWords);

                try {
                    if (titleWords.size() > 1) {
                        bq.add(qp.parse(analyzedTitle), BooleanClause.Occur.MUST);
                        // Look for ngrams formed by the title words
                        if (maxShingleSize != -1) {
                            List<String> shingles = tokenizeString(shingleAnalyzer, analyzedTitle);
                            boost = 1f / shingles.size();
                            for (String shingle : shingles) {
                                queryStr = String.format(Locale.US, "(\"%s\"~%d)^%f", shingle, shingleProximity, boost);
                                bq.add(qp.parse(queryStr), BooleanClause.Occur.SHOULD);
                            }
                        }
                    } else {
                        bq.add(qp.parse(analyzedTitle), BooleanClause.Occur.SHOULD);
                        // Look for words with some character difference
                        if (fuzzyThreshold != -1) {
                            int fuzzyness = analyzedTitle.length() < fuzzyThreshold ? 1 : 2;
                            queryStr = String.format("%s~%d", analyzedTitle, fuzzyness);
                            bq.add(qp.parse(queryStr), BooleanClause.Occur.MUST);
                        }
                        // Look for wrong spelled words
                        if (spellChecker != null) {
                            String[] suggestions = spellChecker.suggestSimilar(analyzedTitle, numSuggestions);
                            if (suggestions.length > 0) {
                                boost = 1.0f / numSuggestions;
                                queryStr = String.format(Locale.US, "(%s)^%f", String.join(" ", suggestions), boost);
                                bq.add(qp.parse(queryStr), BooleanClause.Occur.SHOULD);
                            }
                        }
                    }
                    // Look for synonyms
                    if (synAnalyzer != null) {
                        List<String> synonyms = tokenizeString(synAnalyzer, analyzedTitle);
                        if (synonyms.size() > 0) {
                            boost = 1f / synonyms.size();
                            queryStr = String.format(Locale.US, "(%s)^%f", String.join(" ", synonyms), boost);
                            bq.add(qp.parse(queryStr), BooleanClause.Occur.SHOULD);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Only partial query has been built due to:\n");
                    e.printStackTrace();
                }

                q = bq.build();
                System.out.printf("Query: %s\n", q.toString());

                docs = searcher.search(q, maxDocsRetrieved);
                sd = docs.scoreDocs;

                System.out.printf("Hits: %d (%d limit)\n", docs.totalHits.value, maxDocsRetrieved);

                if (numDocsToRerank != -1 && rerankScoreWeight != -1f) {
                    System.out.printf("Reranking: %d documents\n", numDocsToRerank);
                    try {
                        rerank(t, sd, numDocsToRerank, rerankScoreWeight);
                    } catch (Exception e) {
                        System.out.printf("Reranking aborted due to: %s\n", e.toString());
                    }
                }

                for (int i = 0, n = sd.length; i < n; i++) {
                    docID = reader.storedFields().document(sd[i].doc).get(ParsedDocument.FIELDS.ID);

                    run.printf(Locale.ENGLISH, "%s\tQ0\t%s\t%d\t%.6f\t%s%n", t.getQueryID(), docID, i, sd[i].score,
                            runID);
                }

                run.flush();
                System.out.println();
            }
        } finally {
            if (pythonInterpreter != null)
                pythonInterpreter.close();
            run.close();
            reader.close();
        }
        elapsedTime = System.currentTimeMillis() - start;
        System.out.printf("%d topic(s) searched in %d seconds.%n", topics.length, elapsedTime / 1000);
        System.out.printf("#### Searching complete ####%n");
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        final String indexPath = ConfigProperties.getIndexPath();

        final Similarity sim = ConfigProperties.getSimilarity();

        final String runPath = "experiment";
        final String runID = ConfigProperties.getRunID();
        final int maxDocsRetrieved = 1000;

        final String topicsFile = ConfigProperties.getTopicsFile();
        final int expectedTopics = 672;

        Searcher s = new Searcher(sim, indexPath, topicsFile, expectedTopics, runID, runPath, maxDocsRetrieved);
        s.search();
    }

}
