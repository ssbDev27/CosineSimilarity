
import java.util.*;

public class Dictionary {

    private Hashtable<String, Double> idfs = new Hashtable<>();

    public Hashtable<String, Set<String>> getInvertedIndex() {
        return invertedIndex;
    }

    private Hashtable<String, Set<String>> invertedIndex = new Hashtable<>();

    public Dictionary(List<JavaClass> javaClasses) {
        this.invertedIndex = buildInvertedIndex(javaClasses);
        this.idfs = calculateIdfs(javaClasses.size());
    }

    public Hashtable<String, Double> getIdfs() {
        return idfs;
    }

    /**
     * Build the inverted index for the given Java files.
     * An inverted index is an index data structure storing a mapping from content, such as word,
     * to a set of documents that containing this content. In simple words,
     * it is a hashmap like data structure that directs you from a word to (a) document(s) that contain this word.
     * For example, we have three Java files:
     * a.java has tokens [“for”, “i”, “i”, “hello”]
     * b.java has tokens [“for”, “hello”]
     * c.java has tokens ["i"]
     * the invered index would be hashtable, key is the token, value are the documents containing this token, like
     * {"for":{"a.java", "b.java"},
     * "i":{"a.java", "b.java},
     * "hello":{"a.java", "c.java"}}
     * @param javaClasses
     * @return invertedIndex
     */
    public Hashtable<String, Set<String>> buildInvertedIndex(List<JavaClass> javaClasses){
        Hashtable<String, Set<String>> invertedIndex = new Hashtable<>();
        for (JavaClass i:javaClasses){
            for (String term:i.getTerms()){
                try{
                    invertedIndex.get(term).add(i.getFileName());
                }
                catch(Exception e){
                    invertedIndex.put(term, new HashSet<>(Arrays.asList(i.getFileName())));
                }
            }

        }
        return invertedIndex;
    }

    /**
     * calculate idfs for each term - log(N/m) - N - documents count, m - number of documents containing given term
     * the return is a hashtable, value is the token, value is the token's idf.
     * @param numberOfDocs
     * @return idfs
     */
    public Hashtable<String, Double> calculateIdfs(int numberOfDocs) {
        Hashtable<String, Double> idfs = new Hashtable<>();
        double single_idf=0;
        double intermediate_division=0;
        for (String key:invertedIndex.keySet()){
            double double_N= numberOfDocs;
            double double_n=invertedIndex.get(key).size();
            intermediate_division=double_N/double_n;
            single_idf=Math.log(intermediate_division);  //numberOfDocs=N,  invertedIndex.get(key).size()= size of set(n)

            idfs.put(key,single_idf);
        }
        return idfs;
    }

}
