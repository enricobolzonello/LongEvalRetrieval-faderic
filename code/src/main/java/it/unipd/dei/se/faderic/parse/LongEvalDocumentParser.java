package it.unipd.dei.se.faderic.parse;

import it.unipd.dei.se.faderic.utils.ConfigProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a very basic parser for the LongEval corpus. <p>
 * It is based on the parser <a href="https://github.com/isoboroff/trec-demo/blob/master/src/TrecDocIterator.java"
 * target="_blank">TrecDocIterator.java</a> by Ian Soboroff.
 */
public class LongEvalDocumentParser extends it.unipd.dei.se.faderic.parse.DocumentParser {

    /**
     * The size of the buffer for the body element.
     */
    private static final int BODY_SIZE = 1024 * 8;

    /**
     * The currently parsed document
     */
    private it.unipd.dei.se.faderic.parse.ParsedDocument document = null;

    /**
     * Creates a new LongEval Corpus document parser.
     *
     * @param in the reader to the document(s) to be parsed.
     * @throws NullPointerException     if {@code in} is {@code null}.
     * @throws IllegalArgumentException if any error occurs while creating the
     *                                  parser.
     */
    public LongEvalDocumentParser(final Reader in) {
        super(new BufferedReader(in));
    }

    @Override
    public boolean hasNext() {
        String id = null;
        final StringBuilder body = new StringBuilder(BODY_SIZE);
        try {
            String line;
            Pattern docno_tag = Pattern.compile("<DOCNO>\\s*(\\S+)\\s*<");
            boolean in_doc = false;
            while (true) {
                line = ((BufferedReader) in).readLine();
                if (line == null) {
                    next = false;
                    break;
                }
                if (!in_doc) {
                    if (line.startsWith("<DOC>")) {
                        in_doc = true;
                    } else {
                        continue;
                    }
                }
                if (line.startsWith("</DOC>")) {
                    in_doc = false;
                    body.append(line);
                    break;
                }
                Matcher m = docno_tag.matcher(line);
                if (m.find()) {
                    id = m.group(1);
                }
                body.append(line).append(" ");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse the document.", e);
        }
        if (id != null) {
            document = new it.unipd.dei.se.faderic.parse.ParsedDocument(id,
                    body.length() > 0 ? body.toString().replaceAll("<[^>]*>", " ") : "#");
        }
        return next;
    }

    @Override
    protected final it.unipd.dei.se.faderic.parse.ParsedDocument parse() {
        return document;
    }

    /**
     * Main method of the class. Just for testing purposes.
     *
     * @param args command line arguments.
     * @throws Exception if something goes wrong while indexing.
     */
    public static void main(String[] args) throws Exception {

        final String docsPath = ConfigProperties.getDocsPath();
        Reader reader = Files.newBufferedReader(
            Paths.get(docsPath + "/collector_kodicare_1.txt"), StandardCharsets.UTF_8);

        LongEvalDocumentParser p = new LongEvalDocumentParser(reader);

        for (it.unipd.dei.se.faderic.parse.ParsedDocument d : p) {
            System.out.printf("%n%n------------------------------------%n%s%n%n%n", d.toString());
        }

    }

}
