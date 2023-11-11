package it.unipd.dei.se.faderic.analyze;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.opennlp.OpenNLPPOSFilter;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizer;

import it.unipd.dei.se.faderic.utils.ConfigProperties;

/**
 * The {@code SynonymPOSAnalyzer} class is a Java class that extends the {@code Analyzer} class and uses OpenNLP
 * {@code OpenNLPTokenizer} and {@code OpenNLPPOSFilter} to analyze text and also includes a custom synonym filter.
 */
public class SynonymPOSAnalyzer extends Analyzer {

    private Analyzer analyzer;

    /**
     * Constructor for the class
     * 
     * @param analyzer The {@code Analyzer} that will be used to process found synonyms
     */
    public SynonymPOSAnalyzer(Analyzer analyzer) {
        super();
        this.analyzer = analyzer;
    }
    
    /**
     * This function creates a token stream with OpenNLP tokenizer, POS filter, and synonym filter.
     * 
     * @param fieldName The name of the field being analyzed.
     * @return The method {@code createComponents} returns a {@code TokenStreamComponents} object, which contains
     * the tokenizer and token filters used for analyzing text in a specific field.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = null;
        try {
            tokenizer = new OpenNLPTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
                    loadSentenceDetectorModel("opennlp/en-sent.bin"),
                    loadTokenizerModel("opennlp/en-token.bin"));
        } catch (IOException e) {
            throw new IllegalStateException(
                String.format("Unable to create the OpenNLPTokenizer: %s.", e.getMessage()), e);
        }
        TokenStream tokens = new OpenNLPPOSFilter(tokenizer, loadPosTaggerModel("opennlp/en-pos-maxent.bin"));
        tokens = new SynonymPOSFilter(tokens, analyzer);
        return new TokenStreamComponents(tokenizer, tokens);
    }

    /**
     * Main method of the class. Just for testing purposes.
     * 
     * @param args command line arguments.
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void main(String[] args) throws IOException {
        System.out.println(tokenizeString(new SynonymPOSAnalyzer(ConfigProperties.getAnalyzer()), "office"));
    }
}