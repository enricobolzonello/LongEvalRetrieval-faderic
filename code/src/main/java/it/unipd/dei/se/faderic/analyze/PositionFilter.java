package it.unipd.dei.se.faderic.analyze;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * The PositionFilter class is a TokenFilter that adjusts the position increment of tokens in a
 * TokenStream.
 */
public final class PositionFilter extends TokenFilter {
    
    private final int positionIncrement;

    private PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    /**
     * Constructor for the class
     * 
     * @param input The {@code TokenStream} to be processed
     * @param positionIncrement The {@code PositionIncrementAttribute} value to set for the tokens
     */
    public PositionFilter(TokenStream input, int positionIncrement) {
        super(input);
        if (positionIncrement < 0)
            throw new IllegalArgumentException("positionIncrement may not be negative");
        this.positionIncrement = positionIncrement;
    }

    /**
     * Constructor for the class with default behavior, that is using the {@code positionIncrement} equal to {@code 1}
     * 
     * @param input The TokenStream to be processed
     */
    public PositionFilter(TokenStream input) {
        this(input, 1);
    }

    /**
     * This function increments the token position if the position increment attribute is not equal to
     * zero.
     * 
     * @return The method {@code incrementToken()} returns a boolean value. It returns {@code true} if there is
     * another token available, and {@code false} if there are no more tokens.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            if (posIncrAtt.getPositionIncrement() != 0)
                posIncrAtt.setPositionIncrement(positionIncrement);
            return true;
        }
        return false;
    }
}