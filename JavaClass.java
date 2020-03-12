import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

import static javax.lang.model.SourceVersion.isIdentifier;


public class JavaClass {
    private String content;
    private String fileName;
    private List<String> terms;

    public Hashtable<String, Double> getTfs() {
        return tfs;
    }

    private Hashtable<String, Double> tfs = new Hashtable<>();
    private List<Double> tfIdfs = new ArrayList<>();


    public JavaClass(String fileName, File file) {
        this.fileName = fileName;
        this.content = readDocument(file);
        preprocessDocument();
        this.tfs = calculateTfs(terms);
    }

    public JavaClass() {
    }

    public void setTfIdfs(List<Double> tfIdfs) {
        this.tfIdfs = tfIdfs;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getTerms() {
        return terms;
    }

    public List<Double> getTfIdfs() {
        return tfIdfs;
    }

    /**
     * read the content in a Java file into a String and store it to the variable *content*.
     * @param file . A file Object
     * @return content. A string storing the content of the Java file.
     */
    public String readDocument(File file) {
        BufferedReader buf=null;
        String line="";
        try {
            InputStream is = new FileInputStream(file);
            buf = new BufferedReader(new InputStreamReader(is));
            line = buf.readLine();
        }
        catch(Exception e){
            System.out.println("oops! file reading issue");
        }

        StringBuilder sb = new StringBuilder();
        while(line != null)
        {
            sb.append(line).append("\n");
            try {
                line = buf.readLine();
            }
            catch(Exception e){
                System.out.println("oops! readline issue");
            }

        }
        String content = sb.toString();
        return content;
    }

    /**
     * Preprocess the Java class file and store the processed tokens in the variable *terms*.
     */
    public void preprocessDocument() {
        List<String> tokens = tokenizeAndStemDocument();
        terms = removePunctuation(tokens);
    }

    /**
     * Tokenize the *content* of the Java file, return a list of tokens.
     * It contains two three steps:
     * 1. split the content into tokens, this is already done.
     * 2. do the Lemmatization on the tokens
     * 3. deal with the CamelCase.
     * @return tokens
     */
    private List<String> tokenizeAndStemDocument() {

        List<String> tokens = new ArrayList<>();
        CoreDocument coreDocument = new CoreDocument(content);
        // annotate document for tokenization
        Utility.pipeline.annotate(coreDocument);
        for (CoreSentence sentense: coreDocument.sentences()){
            for (CoreLabel token: sentense.tokens()){
                String tokenLemma=""; //do the Lemmatization and get the lemma of the token.
                tokenLemma=token.lemma();
                List<String> tokenSplits = splitIdentifiers(tokenLemma); // deal with the CamelCases.
                tokens.addAll(tokenSplits);
            }
        }
        return tokens;
    }


    /**
     * This method take a string as input and return a list of tokens after splitting the CamelCase
     * For example, given the String,
     * 1. CamelCase, it should return "Camel" and "Case".
     * 2. camelCase, return "camel" and "Case".
     * 3. MAXNumber, return "MAX" and "Number".
     * 4. top1Results. return "top", "1" and "Results"
     * @param tokenLemma
     * @return a list of tokens after spliting the camel case
     */
    private List<String> splitIdentifiers(String tokenLemma){
        List<String> splitTokens = new ArrayList<>();

        for (String i: StringUtils.splitByCharacterTypeCamelCase(tokenLemma)){
            splitTokens.add(i);
        }
        return splitTokens;
    }


    /**
     *  Loop the tokens in the Java file and return a token list without the punctuations.
     * @param tokens
     * @return tokensWithoutPunctuation
     */
    private List<String> removePunctuation(List<String> tokens){
        List<String> tokensWithoutPunctuation = new ArrayList<>();
        for (String token: tokens){
            if (isIdentifier(token)){
                tokensWithoutPunctuation.add(token);
            }
        }
        return tokensWithoutPunctuation;
    }

    /**
     * Calculate TFs of the Java class. The TF should be store in the variable *tfs*. The key is the term, the value is
     * the frequency of this term. The method need to process the tokens in the variable *terms*. For example, the
     * variable *terms* is [“for”, “i”, “i”,“i”, “printf”, “hello”] ,
     * the output should be a hashtable with the value {“for”:1, “i”:3, “printf”:1, “hello”:1}
     *
     * @param terms a list of terms in the Java file.
     * @return tfs.
     */
    private Hashtable<String, Double> calculateTfs( List<String> terms) {
        Hashtable<String, Double> tfs = new Hashtable<>();
        for (String s: terms) {
            tfs.put(s,Double.valueOf(Collections.frequency(terms, s)));
        }
        return tfs;
    }

    /**
     * This method is used for calculating the TF.IDF representation of the Java file.
     * It accepts a dictionary which contains the IDF information.
     * And it returns results as list of TF-IDF values for terms in the same order as dictionary.getIdfs()
     * @param dictionary
     * @return tfIdfs. list of TF-IDF values for terms
     */
    public List<Double>  calculateTfIdfs(Dictionary dictionary) {
        List<Double> tfIdfs = new ArrayList<>();
        Hashtable<String, Double> Idfs=dictionary.getIdfs();
        Double Term_total = 0.0;
        for(String key:tfs.keySet())
            Term_total += tfs.get(key);

        for (String term:dictionary.getIdfs().keySet()){
            double single_tfidf=0.00;
            try {
                single_tfidf = (tfs.get(term)/Term_total) * Idfs.get(term);
            }
            catch (Exception e){
                single_tfidf = 0;
            }
            tfIdfs.add(single_tfidf);
        }
        return tfIdfs;
    }

    /**
     * Do the calculation of the cosine similarity between this document and the given document.
     * @param query
     * @return the cosine similarity between the TFIDF representation of this Java file and that of the given Java file.
     */
    public double calculateCosineSimilarity(JavaClass query) {
        double cosineSimilarity = 0;
        double dotproduct=0;
        double d1=0;
        double d2=0;
        for (int i = 0; i < tfIdfs.size(); i++){
            dotproduct+=this.tfIdfs.get(i)*query.tfIdfs.get(i);
            d1+=Math.pow(this.tfIdfs.get(i),2);
            d2+=Math.pow(query.tfIdfs.get(i),2);
        }
        cosineSimilarity=dotproduct/(Math.sqrt(d1)*Math.sqrt(d2));
        return cosineSimilarity;
    }
}
