package it.unipd.dei.se.faderic.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spell.PlainTextDictionary;

import it.unipd.dei.se.faderic.analyze.EnglishLongEvalAnalyzer;
import it.unipd.dei.se.faderic.analyze.FrenchLongEvalAnalyzer;
import it.unipd.dei.se.faderic.analyze.SynonymAnalyzer;
import it.unipd.dei.se.faderic.analyze.SynonymPOSAnalyzer;

import static it.unipd.dei.se.faderic.analyze.AnalyzerUtil.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * The {@code ConfigProperties} class is used to load and retrieve properties from a configuration file for a
 * search engine application.
 */
public class ConfigProperties {

    /**
     * The class loader of this class. Needed for reading files from the
     * {@code resource} directory.
     */
    private static final ClassLoader CL = ConfigProperties.class.getClassLoader();

    /**
     * Defining an enumeration type {@code Language} for the language options.
     */
    public enum Language {
        /**
         * French language option
         */
        French,

        /**
         * English language option
         */
        English;
    }

    /**
     * The {@code SimilarityName} enum is defining a set of possible values for the type of similarity to be
     * used in this system.
     */
    public enum SimilarityName {
        /**
         * Okapi {@code BM25Similarity} option
         */
        BM25,

        /**
         * {@code LMDirichletSimilarity} option
         */
        LMDirichlet;
    }

    /**
     * The {@code StemmerName} enum is defining a set of possible values for the type of stemmer to be used
     * in this system.
     */
    public enum StemmerName {
        /**
         * Snowball {@code FrenchStemmer} option
         */
        SnowballFrench,

        /**
         * {@code FrenchLightStemmer} option
         */
        Light,

        /**
         * Snowball {@code EnglishStemmer} (Porter2) option
         */
        SnowballEnglish,

        /**
         * {@code KStemmer} option
         */
        Krovetz;
    }

    /**
     * The name of the config file in the resources
     */
    private static String CONFIG_FILE = "config.xml";

    /**
     * The {@code Properties} read from the config file
     */
    private static Properties prop = loadProperties(CONFIG_FILE);

    /**
     * The language chosen in the config file for the system
     */
    public static Language lang = loadLanguage();

    /**
     * The function loads properties from an XML file and returns them as a {@code Properties} object.
     * 
     * @param filePath A string representing the file path of the XML file containing the properties to
     * be loaded.
     * @return The method is returning a {@code Properties} object that contains the properties loaded from an
     * XML file located at the specified file path.
     */
    private static Properties loadProperties(String filePath) {
        Properties prop = new Properties();
        try {
            prop.loadFromXML(CL.getResourceAsStream(filePath));
            return prop;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(String.format("Unable to find file %s: %s", filePath, e.getMessage()), e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Error while reading from the input stream: %s", e.getMessage()), e);
        }
    }

    /**
     * This function loads a language based on a property value and throws an exception if the value is
     * blank or not available.
     * 
     * @return The method is returning a {@code Language} enum value.
     */
    private static Language loadLanguage() {
        String langStr = prop.getProperty("general.language");
        if (StringUtils.isBlank(langStr))
            throw new IllegalStateException("general.language property is mandatory");
        try {
            return Language.valueOf(langStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("'%s' language is not available", langStr));
        }
    }

    /**
     * This function retrieves the document path from a properties file and throws an exception if it
     * is blank.
     * 
     * @return The method is returning a {@code String} value which is the value of the
     * {@code indexer.<lang>.docsPath} property from a properties file.
     */
    public static String getDocsPath() {
        String docsPath = prop.getProperty("indexer." + lang.name() + ".docsPath");
        if (StringUtils.isBlank(docsPath))
            throw new IllegalStateException("indexer." + lang.name() + ".docsPath property is mandatory");
        return docsPath;
    }

    /**
     * This function retrieves the index path from a properties file and throws an exception if it is
     * blank.
     * 
     * @return The method is returning a {@code String} value which is the value of the
     * {@code "indexer.<lang>.indexPath"} property from a properties file.
     */
    public static String getIndexPath() {
        String indexPath = prop.getProperty("indexer." + lang.name() + ".indexPath");
        if (StringUtils.isBlank(indexPath))
            throw new IllegalStateException("indexer." + lang.name() + ".indexPath property is mandatory");
        return indexPath;
    }

    /**
     * This function returns a similarity object based on the value of a property, either BM25 or
     * LMDirichlet, with optional parameters.
     * 
     * @return The method is returning an instance of the {@code Similarity} interface, which is either a
     * {@code BM25Similarity} or a {@code LMDirichletSimilarity} object, depending on the value of the
     * "general.similarity" property in the prop object.
     */
    public static Similarity getSimilarity() {
        String similarityName = prop.getProperty("general.similarity");
        switch (SimilarityName.valueOf(similarityName)) {
            case BM25:
                try {
                    float k1 = 1.2f, b = 0.75f;
                    String k1Str = prop.getProperty("general.BM25.k1"), bStr = prop.getProperty("general.BM25.b");;
                    if (!StringUtils.isBlank(k1Str))
                        k1 = Float.parseFloat(k1Str);
                    if (!StringUtils.isBlank(bStr))
                        b = Float.parseFloat(bStr);
                    return new BM25Similarity(k1, b);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("BM25 parameters are invalid.", e);
                }
            case LMDirichlet:
                try {
                    float mu = 2000f;
                    String muStr = prop.getProperty("general.LMDirichlet.mu");
                    if (!StringUtils.isBlank(muStr))
                        mu = Float.parseFloat(muStr);
                    return new LMDirichletSimilarity(mu);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("LMDirichlet parameter is invalid.", e);
                }
            default:
                throw new IllegalStateException("general.similarity property is missing or invalid");
        }
    }

    /**
     * This function returns an {@code Analyzer} based on the language specified, with an option to use a stop
     * filter.
     * 
     * @param useStopFilter A boolean value that determines whether or not to use a stop filter in the
     * analyzer.
     * @return An instance of either {@code EnglishLongEvalAnalyzer} or {@code FrenchLongEvalAnalyzer}
     * depending on the value of the boolean parameter {@code useStopFilter}.
     */
    public static Analyzer getAnalyzer(boolean useStopFilter) {
        switch (lang) {
            case English:
                return new EnglishLongEvalAnalyzer(useStopFilter);
            default:
                return new FrenchLongEvalAnalyzer(useStopFilter);
        }
    }

    /**
     * The function returns an {@code Analyzer} object with a default setting of {@code true} for the 
     * {@code useStopFilter} parameter.
     * 
     * @return An instance of either {@code EnglishLongEvalAnalyzer} or {@code FrenchLongEvalAnalyzer}
     */
    public static Analyzer getAnalyzer() {
        return getAnalyzer(true);
    }

    /**
     * This function returns a set of stop words based on the language specified in the properties file
     * or defaults to English or French.
     * 
     * @return The method is returning a {@code CharArraySet} object, which is either the default stop set for
     * English or French language, or a custom stop set loaded from a file specified in a properties
     * file.
     */
    public static CharArraySet getStopSet() {
        String stopFile = prop.getProperty("analyzer." + lang.name() + ".stopFile");
        if (StringUtils.isBlank(stopFile))
            switch (lang) {
                case English:
                    return EnglishAnalyzer.getDefaultStopSet();
                default:
                    return FrenchAnalyzer.getDefaultStopSet();
            }
        return loadStopList(stopFile);
    }

    /**
     * This function retrieves the stemmer name from a properties file and returns it as an enum value,
     * throwing an exception if the stemmer name is not available for the specified language.
     * 
     * @return The method is returning a value of type {@code StemmerName}.
     */
    public static StemmerName getStemmerName() {
        String stemmerName = prop.getProperty("analyzer." + lang.name()+ ".stemmer");
        if (StringUtils.isBlank(stemmerName))
            throw new IllegalStateException("analyzer." + lang.name() + ".stemmer property is mandatory");
        try {
            return StemmerName.valueOf(stemmerName);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                String.format("%s stemmer is not available in language %", stemmerName, lang.name()));
        }
    }

    /**
     * This function retrieves the topics file path from a properties file based on the language and
     * throws an exception if the property is not found.
     * 
     * @return The method is returning a {@code String} value which is the value of the
     * {@code searcher.<lang>.topicsFile} property from a properties file.
     */
    public static String getTopicsFile() {
        String topicsFile = prop.getProperty("searcher." + lang.name() + ".topicsFile");
        if (StringUtils.isBlank(topicsFile))
            throw new IllegalStateException("searcher." + lang.name() + ".topicsFile property is mandatory");
        return topicsFile;
    }

    /**
     * This function retrieves a run ID from a properties file and throws an exception if it is blank.
     * 
     * @return The method is returning a String value which is the runID obtained from the properties
     * file.
     */
    public static String getRunID() {
        String runID = prop.getProperty("searcher." + lang.name() + ".runID");
        if (StringUtils.isBlank(runID))
            throw new IllegalStateException("searcher." + lang.name() + ".runID property is mandatory");
        return runID;
    }

    /**
     * This function retrieves the maximum shingle size from a properties file and validates that it is
     * an integer greater than or equal to {@code 2}.
     * 
     * @return The method returns an integer value which represents the maximum shingle size. If the
     * value of the property {@code searcher.maxShingleSize} is not specified or is blank, it returns {@code -1}.
     * If the value is specified but is not a valid integer or is less than {@code 2}, it throws an
     * {@code IllegalStateException}. Otherwise, it returns the integer value of the property.
     */
    public static int getMaxShingleSize() {
        String maxShingleSizeStr = prop.getProperty("searcher.maxShingleSize");
        if (StringUtils.isBlank(maxShingleSizeStr))
            return -1;
        try {
            int maxShingleSize = Integer.parseInt(maxShingleSizeStr);
            if (maxShingleSize < 2)
                throw new IllegalStateException("The value of property searcher.maxShingleSize has to be >= 2");
            return maxShingleSize;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("The value of property searcher.maxShingleSize is not Integer: %s", e.getMessage()), e);
        }
    }

    /**
     * This function retrieves and validates a fuzzy threshold value from a properties file.
     * 
     * @return The method returns an integer value representing the fuzzy threshold. If the value of
     * the property {@code searcher.fuzzyThreshold} is blank, it returns {@code -1}.
     * If the value is not an integer or is less than {@code 1}, it throws an {@code IllegalStateException}
     * with an appropriate message. Otherwise, it returns the parsed integer value of the property.
     */
    public static int getFuzzyThreshold() {
        String fuzzyThresholdStr = prop.getProperty("searcher.fuzzyThreshold");
        if (StringUtils.isBlank(fuzzyThresholdStr))
            return -1;
        try {
            int fuzzyThreshold = Integer.parseInt(fuzzyThresholdStr);
            if (fuzzyThreshold < 1)
                throw new IllegalStateException("The value of property searcher.fuzzyThreshold has to be >= 1");
            return fuzzyThreshold;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("The value of property searcher.fuzzyThreshold is not Integer: %s", e.getMessage()), e);
        }
    }    

    /**
     * This function returns a synonym analyzer based on the language and configuration properties.
     * 
     * @return The method is returning an instance of the {@code Analyzer} class, either a {@code SynonymPOSAnalyzer}
     * or a {@code SynonymAnalyzer} depending on certain conditions.
     */
    public static Analyzer getSynonymAnalyzer() {
        String usePOSSynonymsStr = prop.getProperty("searcher.English.usePOSSynonyms");
        boolean usePOSSynonyms = Boolean.parseBoolean(usePOSSynonymsStr);
        if (lang == Language.English && usePOSSynonyms)
            return new SynonymPOSAnalyzer(ConfigProperties.getAnalyzer());
        else {
            String synonymFile = prop.getProperty("searcher." + lang.name() + ".synonymFile");
            if (StringUtils.isBlank(synonymFile))
                return null;
            try {
                SynonymMap synonyms = loadSynonymList(synonymFile, getAnalyzer(false));
                return new SynonymAnalyzer(synonyms, getStopSet());
            } catch (Exception e) {
                throw new IllegalStateException("Error while creating SynonymAnalyzer", e);
            }
        }
    }

    /**
     * This function retrieves a spell check dictionary based on a specified language.
     * 
     * @return The method is returning a {@code PlainTextDictionary} object.
     */
    public static PlainTextDictionary getSpellCheckDict() {
        String spellCheckFile = prop.getProperty("searcher." + lang.name() + ".spellCheckFile");
        if (StringUtils.isBlank(spellCheckFile))
            return null;
        try {
            return loadDictionary(spellCheckFile);
        } catch (Exception e) {
            throw new IllegalStateException("Error while reading spellcheck dict", e);
        }
    }

    /**
     * This function returns the value of the rerank model name property.
     * 
     * @return A {@code String} representing the rerank model name
     */
    public static String getRerankBERTModel() {
        return prop.getProperty("searcher.rerankBERTModel");
    }

    /**
     * This function retrieves the number of documents to be re-ranked from a configuration file and
     * validates that it is an integer greater than or equal to {@code 2}.
     * 
     * @return The method returns an integer value representing the number of documents to be reranked.
     * If the value is not specified or is less than {@code 2}, it returns {@code -1}.
     */
    public static int getNumDocsToRerank() {
        String numDocsToRerankStr = prop.getProperty("searcher.numDocsToRerank");
        if (StringUtils.isBlank(numDocsToRerankStr))
            return -1;
        try {
            int numDocsToRerank = Integer.parseInt(numDocsToRerankStr);
            if (numDocsToRerank < 2)
                throw new IllegalStateException("The value of property searcher.numDocsToRerank has to be >= 2");
            return numDocsToRerank;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("The value of property searcher.numDocsToRerank is not Integer: %s", e.getMessage()), e);
        }
    }

    /**
     * This function retrieves and validates a float value for the rerank score weight from a
     * properties file.
     * 
     * @return The method returns a float value representing the weight of the rerank score. If the
     * value is not specified or is invalid, it returns {@code -1f}.
     */
    public static float getRerankScoreWeight() {
        String rerankScoreWeightStr = prop.getProperty("searcher.rerankScoreWeight");
        if (StringUtils.isBlank(rerankScoreWeightStr))
            return -1f;
        try {
            float rerankScoreWeight = Float.parseFloat(rerankScoreWeightStr);
            if (rerankScoreWeight <= 0f || rerankScoreWeight > 1f)
                throw new IllegalStateException("The value of property searcher.rerankScoreWeight has to be in (0.0, 1.0]");
            return rerankScoreWeight;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("The value of property searcher.rerankScoreWeight is not Float: %s", e.getMessage()), e);
        }
    }

    /**
     * This function returns a boolean value based on whether the reranking feature is available
     * i.e., all the rerank parameters are correctly set in the properties.
     * 
     * @return A boolean value is being returned.
     */
    public static boolean useReranking() {
        return StringUtils.isNotBlank(getRerankBERTModel())
                && getNumDocsToRerank() != -1
                && getRerankScoreWeight() != -1f;
    }

    /**
     * Main method of the class. Just for testing purposes.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println(String.format("The active language is %s", ConfigProperties.lang));
    }
}
