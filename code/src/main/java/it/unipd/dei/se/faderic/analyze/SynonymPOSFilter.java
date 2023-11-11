package it.unipd.dei.se.faderic.analyze;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.tokenizeString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code SynonymPOSFilter} class is a Java implementation of a token filter that uses WordNet to find
 * synonyms of input tokens based on their part-of-speech tags.
 */
public class SynonymPOSFilter extends TokenFilter {

    private Dictionary d;
    private Analyzer analyzer;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAttr = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private final LinkedList<String> extraTokens = new LinkedList<String>();
    private State savedState;

    /**
     * Constructor for the class
     * 
     * @param input The {@code TokenStream} to be processed
     * @param analyzer The {@code Analyzer} that will be used to process found synonyms
     */
    public SynonymPOSFilter(TokenStream input, Analyzer analyzer) {
        super(input);
        try {
            d = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            throw new IllegalStateException(
                    "Error occurred while loading WordNet dictionary", e);
        }
        this.analyzer = analyzer;
    }

    
    /**
     * This function checks if there are any extra tokens, and if so, it sets the position increment to
     * 0 and appends the next extra token; otherwise, it gets synonyms for the current token, saves the
     * state, and returns true if there are more tokens.
     * 
     * @return The method {@code incrementToken()} returns a boolean value indicating whether there are more
     * tokens to be processed or not. If there are more tokens, it returns {@code true}, otherwise it returns
     * {@code false}.
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (!extraTokens.isEmpty()) {
            restoreState(savedState);
            posIncAtt.setPositionIncrement(0);
            termAtt.setEmpty().append(extraTokens.remove());
            return true;
        }
        if (input.incrementToken()) {
            extraTokens.addAll(getSynonyms(termAtt.toString(), typeAttr.type()));
            if (!extraTokens.isEmpty())
                termAtt.setEmpty().append(extraTokens.remove());
            savedState = captureState();
            return true;
        }
        return false;
    }

    /**
     * The function takes a word and its part of speech tag as input, and returns a list of synonyms
     * for that word.
     * 
     * @param token The word for which synonyms are to be found.
     * @param tagStr A {@code String} that represents the part of speech tag of the
     * token. It is used to determine the appropriate WordNet POS tag to use when looking up synonyms.
     * @return A list of synonyms for a given token, based on its part of speech tag.
     */
    private List<String> getSynonyms(String token, String tagStr) {
        List<String> synonyms = new ArrayList<String>();
        POS tag = null;
        if (tagStr.length() >= 2)
            switch (tagStr.substring(0, 2)) {
                case "JJ":
                    tag = POS.ADJECTIVE;
                    break;
                case "VB":
                    tag = POS.VERB;
                    break;
                case "RB":
                    tag = POS.ADVERB;
                    break;
                case "NN":
                    tag = POS.NOUN;
                    break;
                default:
                    break;
            }
        
        if (tag != null)
            try {
                StringBuilder synonymsStr = new StringBuilder();
                IndexWord indexWord = d.lookupIndexWord(tag, token);
                if (indexWord != null) {
                    for (Synset sense : indexWord.getSenses())
                        for (Word word : sense.getWords())
                            synonymsStr.append(word.getLemma() + " ");
                    
                    synonyms = tokenizeString(analyzer, synonymsStr.toString());                // Analyze synonyms
                    synonyms = synonyms.stream().distinct().collect(Collectors.toList());       // Remove duplicates
                    synonyms.remove(indexWord.getLemma());                                      // Remove original token
                }
            } catch (JWNLException | IOException e) {
                System.out.println(
                        String.format("Error while looking for synonyms of %s: %s", token, e.getMessage()));
            }
        return synonyms;
    }
}