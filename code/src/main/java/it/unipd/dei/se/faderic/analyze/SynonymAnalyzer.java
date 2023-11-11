package it.unipd.dei.se.faderic.analyze;

import it.unipd.dei.se.faderic.utils.ConfigProperties;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

/**
 * The {@code SynonymAnalyzer} class is a Java class that analyzes text by replacing synonyms, removing stop
 * words, and flattening the resulting graph.
 * Since {@code StopFilter} cannot be applied when creating a {@code SynonymMap} (a Token cannot be completely removed), 
 * it is also possible to pass a {@code CharArraySet} stoplist, which is applied to the {@code TokenStream} as last step.
 */
public class SynonymAnalyzer extends Analyzer {

    private SynonymMap synonyms;
    private CharArraySet stoplist;

    /**
     * Constructor for the class
     * 
     * @param synonyms The {@code SynonymMap} that will be used to retrieve synonyms
     * @param stoplist The stoplist that will be applied after having retrieved the synonyms
     */
    public SynonymAnalyzer(SynonymMap synonyms, CharArraySet stoplist) {
        super();
        this.synonyms = synonyms;
        this.stoplist = stoplist;
    }

    /**
     * Constructor for the class using an empty stoplist
     * 
     * @param synonyms The {@code SynonymMap} that will be used to retrieve synonyms
     */
    public SynonymAnalyzer(SynonymMap synonyms) {
        this(synonyms, CharArraySet.EMPTY_SET);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokens = new SynonymGraphFilter(tokenizer, synonyms, false);
        tokens = new FlattenGraphFilter(tokens);
        tokens = new StopFilter(tokens, stoplist);
        return new TokenStreamComponents(tokenizer, tokens);
    }

    /**
     * Main method of the class. Just for testing purposes.
     * 
     * @param args command line arguments.
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void main(String[] args) throws IOException {
        System.out.println(tokenizeString(ConfigProperties.getSynonymAnalyzer(), "deplo"));
    }
}