package it.unipd.dei.se.faderic.parse;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a document parser.
 */
public abstract class DocumentParser
        implements Iterator<it.unipd.dei.se.faderic.parse.ParsedDocument>,
        Iterable<it.unipd.dei.se.faderic.parse.ParsedDocument> {

    /**
     * Indicates whether there is another {@code ParsedDocument} to return.
     */
    protected boolean next = true;

    /**
     * The reader to be used to parse document(s).
     */
    protected final Reader in;

    /**
     * Creates a new document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException if {@code in} is {@code null}.
     */
    protected DocumentParser(final Reader in) {
        if (in == null) {
            throw new NullPointerException("Reader cannot be null.");
        }
        this.in = in;
    }

    @Override
    public final Iterator<it.unipd.dei.se.faderic.parse.ParsedDocument> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return next;
    }

    @Override
    public final it.unipd.dei.se.faderic.parse.ParsedDocument next() {
        if (!next) {
            throw new NoSuchElementException("No more documents to parse.");
        }
        try {
            return parse();
        } finally {
            try {
                // we reached the end of the file
                if (!next) {
                    in.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close the reader.", e);
            }
        }
    }

    /**
     * Creates a new {@code DocumentParser}.
     * <p>
     * It assumes the {@code DocumentParser} has a single-parameter constructor
     * which takes a {@code Reader} as input.
     *
     * @param cls the class of the document parser to be instantiated.
     * @param in  the reader to the document(s) to be parsed.
     * @return a new instance of {@code DocumentParser} for the given class.
     * @throws NullPointerException  if {@code cls} and/or {@code in} are
     *                               {@code null}.
     * @throws IllegalStateException if something goes wrong in instantiating the
     *                               class.
     */
    public static final DocumentParser create(Class<? extends DocumentParser> cls, Reader in) {
        if (cls == null) {
            throw new NullPointerException("Document parser class cannot be null.");
        }

        if (in == null) {
            throw new NullPointerException("Reader cannot be null.");
        }
        try {
            return cls.getConstructor(Reader.class).newInstance(in);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Unable to instantiate document parser %s.", cls.getName()),
                    e);
        }
    }

    /**
     * Performs the actual parsing of the document.
     *
     * @return the parsed document.
     */
    protected abstract it.unipd.dei.se.faderic.parse.ParsedDocument parse();
}
