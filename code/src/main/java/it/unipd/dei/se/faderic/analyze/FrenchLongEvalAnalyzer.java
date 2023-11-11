package it.unipd.dei.se.faderic.analyze;

import it.unipd.dei.se.faderic.utils.ConfigProperties;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.fr.FrenchLightStemFilter;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.tartarus.snowball.ext.FrenchStemmer;

/**
 * The {@code FrenchLongEvalAnalyzer} class is used to create a set of token stream components for analyzing
 * French text, including a tokenizer, filters for folding, elision, stop words, and stemming.
 */
public class FrenchLongEvalAnalyzer extends Analyzer {

    private boolean useStopFilter;

    /**
     * Constructor for the class
     * 
     * @param useStopFilter A boolean value to choose if the analyzer will use the {@code StopFilter}
     */
    public FrenchLongEvalAnalyzer(boolean useStopFilter) {
        super();
        this.useStopFilter = useStopFilter;
    }

    /**
     * Constructor for the class with default behavior, that is using the {@code StopFilter}
     */
    public FrenchLongEvalAnalyzer() {
        this(true);
    }
    
    /**
     * This function creates a set of token stream components for analyzing French text, including a
     * tokenizer, filters for folding, elision, stop words, and stemming.
     * 
     * @param fieldName The name of the field being analyzed.
     * @return A {@code TokenStreamComponents} object is being returned.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokens = new ICUFoldingFilter(tokenizer);
        tokens = new ElisionFilter(tokens, FrenchAnalyzer.DEFAULT_ARTICLES);
        if (useStopFilter)
            tokens = new StopFilter(tokens, ConfigProperties.getStopSet());
        tokens = new PositionFilter(tokens, 1); // Ignore positionIncrement due to removed stopwords

        switch (ConfigProperties.getStemmerName()) {
            case SnowballFrench:
                tokens = new SnowballFilter(tokens, new FrenchStemmer());
                break;
            case Light:
                tokens = new FrenchLightStemFilter(tokens);
                break;
            default:
                break;
        }

        return new TokenStreamComponents(tokenizer, tokens);
    }

    /**
     * Main method of the class. Just for testing purposes.
     * 
     * @param args command line arguments.
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void main(String[] args) throws IOException {
        final String text = "La méthode d'analyse de texte est essentielle pour l'extraction d'informations.";
        System.out.println(tokenizeString(new FrenchLongEvalAnalyzer(), text));
    }
}
