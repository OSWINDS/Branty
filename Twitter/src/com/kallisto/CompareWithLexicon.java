/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class CompareWithLexicon {

    private static String line;

    /**
     * @param args the command line arguments
     */
    //Sugkrish ths SimpleLogistic me thn methodo tou lexikou
    public static void CompareLogisticWithLexicon() {
        BufferedReader br = null;
        ArrayList<String> predictedLogistic = new ArrayList<String>();
        ArrayList<String> predictedLexicon = new ArrayList<String>();
        try {//predictedLogistic
            br = new BufferedReader(new FileReader("testSetWithResults.arff"));
            StringBuilder sb = new StringBuilder();
            line = br.readLine();
            Boolean flag = false;
            while (line != null) {
                if (flag) {
                    sb.append(line);
                    sb.append("\n");
                    if (line != null) {
                        String[] split = line.split(",");
                        predictedLogistic.add(split[split.length - 2]);

                    }
                }
                //otan ftasei sth grammh @data, krata tis grammes sthn panw if
                if (line.contains("@data")) {
                    flag = true;
                }
                line = br.readLine();
            }
            
            
            //Predicted Lexicon
            br = new BufferedReader(new FileReader("lexicon2000.txt"));
            try {
                line = br.readLine();

                while (line != null) {
                    String[] split = line.split("\t");
                    predictedLexicon.add(split[split.length - 1]);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int count = 0;
        for (int i = 0; i < predictedLexicon.size(); i++) {
            if (predictedLexicon.get(i).equalsIgnoreCase(predictedLogistic.get(i))) {
                count++;
            }
        }

        float result = (float) (1.0 * count / predictedLexicon.size());
        System.out.println("Count is " + count + "/" + predictedLexicon.size());
        System.out.println("Similarity is " + (result * 100) + "%");

    }

    //Sygkrish ths methodou tou lexikou me ta hand-annotated gia ipologismo akriveias
    public static void CalculateLexiconPrecision() {
        BufferedReader br = null;
        ArrayList<String> predictedAnnotated = new ArrayList<String>();
        ArrayList<String> predictedLexicon = new ArrayList<String>();
        try {

            //annotated
            br = new BufferedReader(new FileReader("testingset.txt"));
            try {
                line = br.readLine();

                while (line != null) {
                    String[] split = line.split("\t");
                    predictedAnnotated.add(split[split.length - 1]);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }


            //lexicon
            br = new BufferedReader(new FileReader("lexicon2000.txt"));
            try {
                line = br.readLine();

                while (line != null) {
                    String[] split = line.split("\t");
                    predictedLexicon.add(split[split.length - 1]);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(CompareWithLexicon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        int count = 0;
        for (int i = 0; i < predictedLexicon.size(); i++) {
            if (predictedLexicon.get(i).equalsIgnoreCase(predictedAnnotated.get(i))) {
                count++;
            }
        }

        float result = (float) (1.0 * count / predictedLexicon.size());
        System.out.println("Count is " + count + "/" + predictedLexicon.size());
        System.out.println("Similarity is " + (result * 100) + "%");
    }

    public static void main(String[] args) {
//        CompareLogisticWithLexicon();
        CalculateLexiconPrecision();
    }
}