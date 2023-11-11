package it.unipd.dei.se.faderic.analyze;

import it.unipd.dei.se.faderic.utils.ConfigProperties;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.EnglishStemmer;


/**
 * The {@code EnglishLongEvalAnalyzer} class is an implementation of the {@code Analyzer} class in Java that performs
 * text analysis on English text, including tokenization, filtering, and stemming.
 */
public class EnglishLongEvalAnalyzer extends Analyzer {

    private boolean useStopFilter;

    /**
     * Constructor for the class
     * 
     * @param useStopFilter A {@code boolean} value to choose if the analyzer will use the {@code StopFilter}
     */
    public EnglishLongEvalAnalyzer(boolean useStopFilter) {
        super();
        this.useStopFilter = useStopFilter;
    }

    /**
     * Constructor for the class with default behavior, that is using the {@code StopFilter}
     */
    public EnglishLongEvalAnalyzer() {
        this(true);
    }

    /**
     * This function creates a set of token stream components for a given field name, including a
     * tokenizer, filters for folding, possessives, stop words, and stemming.
     * 
     * @param fieldName The name of the field being analyzed.
     * @return A {@code TokenStreamComponents} object is being returned.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokens = new ICUFoldingFilter(tokenizer);
        tokens = new EnglishPossessiveFilter(tokens);
        if (useStopFilter)
            tokens = new StopFilter(tokens, ConfigProperties.getStopSet());
        tokens = new PositionFilter(tokens, 1); // Ignore positionIncrement due to removed stopwords

        switch (ConfigProperties.getStemmerName()) {
            case SnowballEnglish:
                tokens = new SnowballFilter(tokens, new EnglishStemmer());
                break;
            case Krovetz:
                tokens = new KStemFilter(tokens);
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
        final String text = "The text analysis method's importance lies in its role in information extraction.";
        System.out.println(tokenizeString(new EnglishLongEvalAnalyzer(), text));
    }
}