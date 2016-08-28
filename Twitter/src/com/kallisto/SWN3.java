/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

/**
 *
 * @author alarv
 */
import static com.kallisto.Preprocess.preprocessForSentiWordNet;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

public class SWN3 {

    private String pathToSWN = "data" + File.separator + "SentiWordNet_3.0.0.txt";
    private static HashMap<String, Double> _dict;
    private static StanfordCoreNLP pipeline;

    public SWN3() {

        _dict = new HashMap<String, Double>();
        HashMap<String, Vector<Double>> _temp = new HashMap<String, Vector<Double>>();
        try {
            BufferedReader csv = new BufferedReader(new FileReader(pathToSWN));
            String line = "";
            while ((line = csv.readLine()) != null) {
                String[] data = line.split("\t");
                Double score = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);
                String[] words = data[4].split(" ");
                for (String w : words) {
                    String[] w_n = w.split("#");
                    w_n[0] += "#" + data[0];
                    int index = Integer.parseInt(w_n[1]) - 1;
                    if (_temp.containsKey(w_n[0])) {
                        Vector<Double> v = _temp.get(w_n[0]);
                        if (index > v.size()) {
                            for (int i = v.size(); i < index; i++) {
                                v.add(0.0);
                            }
                        }
                        v.add(index, score);
                        _temp.put(w_n[0], v);
                    } else {
                        Vector<Double> v = new Vector<Double>();
                        for (int i = 0; i < index; i++) {
                            v.add(0.0);
                        }
                        v.add(index, score);
                        _temp.put(w_n[0], v);
                    }
                }
            }
            Set<String> temp = _temp.keySet();
            for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
                String word = (String) iterator.next();
                Vector<Double> v = _temp.get(word);
                double score = 0.0;
                double sum = 0.0;
                for (int i = 0; i < v.size(); i++) {
                    score += ((double) 1 / (double) (i + 1)) * v.get(i);
                }
                for (int i = 1; i <= v.size(); i++) {
                    sum += (double) 1 / (double) i;
                }
                score /= sum;
//                String sent = "";
//                if (score >= 0.75) {
//                    sent = "strong_positive";
//                } else if (score > 0.25 && score <= 0.5) {
//                    sent = "positive";
//                } else if (score > 0 && score >= 0.25) {
//                    sent = "weak_positive";
//                } else if (score < 0 && score >= -0.25) {
//                    sent = "weak_negative";
//                } else if (score < -0.25 && score >= -0.5) {
//                    sent = "negative";
//                } else if (score <= -0.75) {
//                    sent = "strong_negative";
//                }
                _dict.put(word, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //core-NLP POS tagger
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        pipeline = new StanfordCoreNLP(props);
    }

    public static Double extract(String word, String pos) {
        return _dict.get(word + "#" + pos);
    }

    public static String coreNLPpos(String wordToAnalyze) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 


        // read some text in the text variable
        String text = "player"; // Add your text here!

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(wordToAnalyze);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                return pos;
                // this is the NER label of the token
//                String ne = token.get(NamedEntityTagAnnotation.class);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);


        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefChainAnnotation.class);
        return null;

    }

    public double getWordSentiment(String word) {
        String wordPOS = coreNLPpos(word);
        if (wordPOS == null || wordPOS.length() < 2) {
            return 0;
        } else if (wordPOS.substring(0, 2).equals("NN")) {//noun
            wordPOS = "n";
        } else if (wordPOS.substring(0, 2).equals("RB")) {//adverb
            wordPOS = "r";
        } else if (wordPOS.substring(0, 2).equals("JJ")) {//adjective
            wordPOS = "a";
        } else if (wordPOS.substring(0, 2).equals("VB")) {//verb
            wordPOS = "v";
        } else {
            return 0;
        }
//        System.out.println();
        //

        Double sentimentOfWord = extract(word, wordPOS);
        if (sentimentOfWord != null) {
            return sentimentOfWord;
        } else {
            return 0;
        }

    }

    public static void main(String args[]) throws Exception {
        SWN3 swn = new SWN3();
        
    }
}
