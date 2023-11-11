package it.unipd.dei.se.faderic.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.benchmark.quality.QualityQuery;

/**
 * Read LongEval topics.
 *
 * <p>
 * Expects this topic format -
 *
 * <pre>
 *   &lt;top&gt;
 *   &lt;num&gt;nnn&lt;/num&gt;
 *   &lt;title&gt;title of the topic&lt;/title&gt;
 *   &lt;/top&gt;

 * </pre>
 *
 * Comment lines starting with '#' are ignored.
 */
public class LongEvalTopicsReader {

    private static final String newline = System.getProperty("line.separator");

    /**
     * Constructor for LongEval's TopicsReader
    */
    public LongEvalTopicsReader() {
        super();
    }

    /**
     * Read quality queries from trec format topics file.
     *
     * @param reader where queries are read from.
     * @return the result quality queries.
     * @throws IOException if cannot read the queries.
     */
    public QualityQuery[] readQueries(BufferedReader reader) throws IOException {
        ArrayList<QualityQuery> res = new ArrayList<>();
        StringBuilder sb;
        try {
            while (null != (sb = read(reader, "<top>", null, false, false))) {
                HashMap<String, String> fields = new HashMap<>();
                // Find id
                sb = read(reader, "<num>", null, true, false);
                String id = sb.substring(sb.indexOf(">") + 1, sb.indexOf("</")).trim();
                // Find title
                sb = read(reader, "<title>", null, true, false);
                String title = sb.substring(sb.indexOf(">") + 1, sb.indexOf("</")).trim();
                // Topic creation
                fields.put("title", title);
                QualityQuery topic = new QualityQuery(id, fields);
                res.add(topic);
            }
        } finally {
            reader.close();
        }
        // Sort result array (by ID)
        QualityQuery[] qq = res.toArray(new QualityQuery[0]);
        Arrays.sort(qq);
        return qq;
    }

    /**
     * Read until finding a line that starts with the specified prefix
    */
    private StringBuilder read(
            BufferedReader reader,
            String prefix,
            StringBuilder sb,
            boolean collectMatchLine,
            boolean collectAll)
            throws IOException {
        sb = (sb == null ? new StringBuilder() : sb);
        String sep = "";
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            if (line.startsWith(prefix)) {
                if (collectMatchLine) {
                    sb.append(sep).append(line);
                    sep = newline;
                }
                break;
            }
            if (collectAll) {
                sb.append(sep).append(line);
                sep = newline;
            }
        }
        // System.out.println("read: " + sb);
        return sb;
    }
}
