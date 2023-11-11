package it.unipd.dei.se.faderic.index;

import it.unipd.dei.se.faderic.parse.ParsedDocument;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

import java.io.Reader;

/**
 * Represents a {@link Field} for containing the body of a document. <p>
 * It is a tokenized field, stored, keeping document id, term frequencies
 * and positions (see {@link IndexOptions#DOCS_AND_FREQS_AND_POSITIONS}.
 */
public class BodyField extends Field {

    /**
     * The type of the document body field
     */
    private static final FieldType BODY_TYPE = new FieldType();

    static {
        BODY_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        BODY_TYPE.setTokenized(true);
        BODY_TYPE.setStored(true);      // Needed in the Searcher to pass document body to the reranker
    }

    /**
     * Create a new field for the body of a document.
     *
     * @param value the contents of the body of a document.
     */
    public BodyField(final Reader value) {
        super(ParsedDocument.FIELDS.BODY, value, BODY_TYPE);
    }

    /**
     * Create a new field for the body of a document.
     *
     * @param value the contents of the body of a document.
     */
    public BodyField(final String value) {
        super(ParsedDocument.FIELDS.BODY, value, BODY_TYPE);
    }

}
