/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author anna
 */
public class MLAnnotator {

    private static MongoDB db;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        db = new MongoDB();
        WEKA weka = new WEKA();

        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        Object[] options = {"Positive",
            "Negative",
            "Neutral",
            "Cancel"};

        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream("random lines training set.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String[] columnDetail;
            int counter = 0;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                counter++;
                // Print the content on the console
                columnDetail = strLine.split("\t");
                String brand = columnDetail[1].toLowerCase();
                String tweet = columnDetail[2];
                double positive = Double.parseDouble(columnDetail[3]);
                double negative = Double.parseDouble(columnDetail[4]);
                double neutral = Double.parseDouble(columnDetail[5]);
                double total = Double.parseDouble(columnDetail[6]);
//                if (total > -0.8) {
//                    continue;//tempfix to add more negatives!
//                }
                if(!weka.getTweetSentiment(strLine).equals("negative")){
                    continue;
                }
                int n = JOptionPane.showOptionDialog(frame,
                        brand + "--> " + tweet + "\n"
                        + "positive:" + positive + ",negative:" + negative + "\n"
                        + "neutral:" + neutral + ",total score:" + total + "\n"
                        + "sentiment by our algorithm:" + weka.getTweetSentiment(strLine) + "\n"
                        + " counter: " + counter,
                        "Branty",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[3]);

                if (n == 0) {
                    writeToFile("tweetsOutput.arff", "'" + StringEscapeUtils.escapeEcmaScript(tweet).replaceAll(brand+" ", "").trim() + "'" + "," + positive + "," + negative + "," + neutral + "," + total + "," + "positive");
                } else if (n == 1) {
                    writeToFile("tweetsOutput.arff", "'" + StringEscapeUtils.escapeEcmaScript(tweet).replaceAll(brand+" ", "").trim() + "'" + "," + positive + "," + negative + "," + neutral + "," + total + "," + "negative");
                } else if (n == 2) {
                    writeToFile("tweetsOutput.arff", "'" + StringEscapeUtils.escapeEcmaScript(tweet).replaceAll(brand+" ", "").trim() + "'" + "," + positive + "," + negative + "," + neutral + "," + total + "," + "neutral");
                }
                System.out.println("Counter:" + counter);
            }

            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    public static void writeToFile(String file, String text) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file), true));
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
        }
    }

    public static void parser() {
        Document doc;
        Elements table = null, brand = null, photo = null, profile = null, list = null;
        String br = null, pho = null, prof = null;
        try {
            doc = Jsoup.connect("http://fanpagelist.com/category/brands/dining/view/list/sort/fans/page2")
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            //table = doc.body().select("section.content table.common-table");
            // brand = table.get(3).select("td.name > a:eq(1)");
            //profile = table.get(3).select("td.name > a:eq(2)");
            //photo = table.get(3).select("td.name img");
            list = doc.select("li.ranking_results");
            for (int i = 0; i < list.size(); i++) {
//                System.out.println(i+" "+list.get(i));
                brand = list.get(i).select("a > span.title");
                profile = list.get(i).select(".profile_follow_action a");

                photo = list.get(i).select("img.ranking_profile_image");

                if (profile == null || profile.text().toString().trim().equals("")) {
                    br = brand.first().text();
                    prof = "keno";
                    pho = photo.first().absUrl("src");
                } else {
                    br = brand.first().text();
                    pho = photo.first().absUrl("src");
                    String followUrl = profile.first().attr("href");
                    prof = "@" + followUrl.substring(followUrl.lastIndexOf("=") + 1);

                }
                //System.out.println(br + "\n" + pho + "\n" + prof);
                BasicDBObject in = new BasicDBObject("name", br).
                        append("imgurl", pho).
                        append("profile", prof).
                        append("category", "Food/Beverages");
                db.insert(in, "Thesis_Branty");

            }
//            brand = doc.select(".ranking_results a > span.title");
//            profile = doc.select(".ranking_results .profile_follow_action a[href]");
//            photo = doc.select(".ranking_results img.ranking_profile_image");
//            printElements(brand, photo, profile);
            //insertElementsToDb(brand, photo, profile);
        } catch (IOException ex) {
            System.out.println("Oups!!");
            Logger
                    .getLogger(MLAnnotator.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void insertElementsToDb(Elements brand, Elements photo, Elements profile) {

        for (int i = 0; i < brand.size(); i++) {
            BasicDBObject doc = new BasicDBObject("name", brand.get(i).text()).
                    append("imgurl", photo.get(i).absUrl("src")).
                    append("profile", profile.get(i).text()).
                    append("category", "Food/Beverages");
            db.insert(doc, "Thesis_Branty");
        }

    }

    public static void printElements(Elements brand, Elements photo, Elements profile) {
        List<String> list = new ArrayList<>();
        System.out.println("--------------------Brands------------------");
        for (int i = 0; i < brand.size(); i++) {
            Element para = brand.get(i);
            list.add(para.text());
            System.out.println(list.get(i));
        }
        list.clear();
        System.out.println("--------------------Photo------------------");
        for (int i = 0; i < photo.size(); i++) {
            Element para = photo.get(i);
            list.add(para.absUrl("src"));
            System.out.println(list.get(i));
        }
        list.clear();
        System.out.println("--------------------profile------------------");
        for (int i = 0; i < profile.size(); i++) {
            Element para = profile.get(i);
            list.add(para.attr("href"));
            System.out.println(list.get(i));
        }
    }
}