/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

/**
 *
 * @author alarv
 */
import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

public class StanfordCoreNlpDemo {

    public static void main(String[] args) throws IOException {
        PrintWriter out;
        if (args.length > 1) {
            out = new PrintWriter(args[1]);
        } else {
            out = new PrintWriter(System.out);
        }
        PrintWriter xmlOut = null;
        if (args.length > 2) {
            xmlOut = new PrintWriter(args[2]);
        }

        StanfordCoreNLP pipeline = new StanfordCoreNLP();
        Annotation annotation;
        annotation = new Annotation("email");

        pipeline.annotate(annotation);
        pipeline.prettyPrint(annotation, out);
//        if (xmlOut != null) {
//            pipeline.xmlPrint(annotation, xmlOut);
//        }

        // An Annotation is a Map and you can get and use the various analyses individually.
        // For instance, this gets the parse tree of the first sentence in the text.
//        out.println();
//        // The toString() method on an Annotation just prints the text of the Annotation
//        // But you can see what is in it with other methods like toShorterString()
//        out.println("The top level annotation");
//        out.println(annotation.toShorterString());
//        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//        if (sentences != null && sentences.size() > 0) {
//            ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(0);
//            out.println("The first sentence is:");
//            out.println(sentence.toShorterString());
//            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
//            out.println();
//            out.println("The first sentence tokens are:");
//            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                ArrayCoreMap aToken = (ArrayCoreMap) token;
//                out.println(aToken.toShorterString());
//            }
//            out.println("The first sentence parse tree is:");
//            tree.pennPrint(out);
//            out.println("The first sentence basic dependencies are:");
//            System.out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString("plain"));
//            out.println("The first sentence collapsed, CC-processed dependencies are:");
//            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//            System.out.println(graph.toString("plain"));
//        }
    }
}
