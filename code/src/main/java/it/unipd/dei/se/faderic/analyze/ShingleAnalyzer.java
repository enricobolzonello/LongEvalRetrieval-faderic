package it.unipd.dei.se.faderic.analyze;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * The {@code ShingleAnalyzer} class is a Java class that creates a token stream for a given text by breaking
 * it into shingles of a specified size.
 */
public class ShingleAnalyzer extends Analyzer {
    
    private int maxShingleSize;

    /**
     * Constructor for the class
     * 
     * @param maxShingleSize The maximum size of the shingles that will be created
     */
    public ShingleAnalyzer (int maxShingleSize) {
        super();
        this.maxShingleSize = maxShingleSize;
    }

    /**
     * This function creates a token stream with shingle filter for a given field name in Java.
     * 
     * @param fieldName The name of the field for which the token stream components are being created.
     * @return A {@code TokenStreamComponents} object is being returned.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokens = new ShingleFilter(tokenizer, maxShingleSize);
        ((ShingleFilter) tokens).setOutputUnigrams(false); // Avoid generating one word shingles
        return new TokenStreamComponents(tokenizer, tokens);
    }

    /**
     * Main method of the class. Just for testing purposes.
     * 
     * @param args command line arguments.
     * @throws IOException if something goes wrong while processing the text.
     */
    public static void main(String[] args) throws IOException {
        final String text = "how are you";
        System.out.println(tokenizeString(new ShingleAnalyzer(3), text));
    }
}