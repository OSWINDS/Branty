/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import static com.kallisto.BrandParser.db;
import static com.kallisto.Preprocess.preprocessForSentiWordNet;
import static com.kallisto.SWN3.coreNLPpos;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.ArrayUtils;
import twitter4j.IDs;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 *
 * @author alarv
 */
public class Misc {

    public static String collectionName = "Thesis_Branty";

    /**
     * @param args the command line arguments
     */
    //        //find brand names
    //        MongoDB db = new MongoDB();
    //        BasicDBObject query = new BasicDBObject();
    //        BasicDBObject field = new BasicDBObject();
    //        field.put("name", 1);
    //        DBCursor cursor = db.findWithField(query, field, "brands");
    //        
    //        while (cursor.hasNext()) {
    //            BasicDBObject obj = (BasicDBObject) cursor.next();
    //            System.out.println(obj.getString("name"));
    //        }
    //        
    //        
    //        //update with tweet
    //        BasicDBObject obj = new BasicDBObject().append("user", "8eos")
    //                .append("tweet","eeeeexw ap ta dontia")
    //                .append("date","13/10/2013");
    //        
    //        db.updateTweets(obj, "Timex");
    public static void writeToFile(String text) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    "tweetsTrainingSet.txt"), true));
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
        }
    }

    /*
     * Creates the training set after characterizing each tweet by SentiWordNet
     */
    private static void createTrainingSet() {
        SWN3 swn = new SWN3();

        MongoDB db = new MongoDB();
        BasicDBObject query = new BasicDBObject("tweets", new BasicDBObject("$exists", true));
        DBCursor find = db.find(query, "brands");
//        int putCount = 0, notPutCount = 0;
        HashMap<String, String> map = new HashMap();
        while (find.hasNext()) {
            BasicDBObject currentBrand = (BasicDBObject) find.next();
            BasicDBList tweets = (BasicDBList) currentBrand.get("tweets");
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);
                String tweetText = (String) tweet.get("tweet");
                String sentence = preprocessForSentiWordNet(tweetText);
//                System.out.println("sentence:"+sentence);
                String[] words = sentence.split(" ");
                Double sum = 0.0;
                int positiveCounter = 0, negativeCounter = 0, neutralCounter = 0;
                for (int j = 0; j < words.length; j++) {
                    String word = words[j];
                    String wordPOS = coreNLPpos(word);
                    if (wordPOS == null || wordPOS.length() < 2) {
                        continue;
                    } else if (wordPOS.substring(0, 2).equals("NN")) {//noun
                        wordPOS = "n";
                    } else if (wordPOS.substring(0, 2).equals("RB")) {//adverb
                        wordPOS = "r";
                    } else if (wordPOS.substring(0, 2).equals("JJ")) {//adjective
                        wordPOS = "a";
                    } else if (wordPOS.substring(0, 2).equals("VB")) {//verb
                        wordPOS = "v";
                    } else {
                        continue;
                    }
//        System.out.println();
                    //

                    Double sentimentOfWord = swn.extract(word, wordPOS);
                    if (sentimentOfWord != null) {
                        if (sentimentOfWord > 0) {
                            positiveCounter++;
                        } else if (sentimentOfWord < 0) {
                            negativeCounter++;
                        } else if (sentimentOfWord == 0) {
                            neutralCounter++;
                        }

                        sum += sentimentOfWord;
                    }

//                    System.out.println("Current word:" + word + ", sentiment:" + sentimentOfWord);
                }
                if (!map.containsKey(sentence)) {
                    writeToFile(tweet.getString("date") + "\t"
                            + currentBrand.getString("name") + "\t"
                            + sentence + "\t"
                            + ((double) 1.0 * positiveCounter / words.length) + "\t"
                            + ((double) 1.0 * negativeCounter / words.length) + "\t"
                            + ((double) 1.0 * neutralCounter / words.length) + "\t"
                            + sum);
                    map.put(sentence, sentence);//to avoid doubles(spam)
                }
//                System.out.println("Total Sentiment: " + sum);
            }

        }
//        System.out.println("put count:" + putCount);
//        System.out.println("not put count:" + notPutCount);
    }

    private static void updateBrandsWithOne() {
        MongoDB db = new MongoDB();
        JFrame frame = new JFrame();
        int n = 0;
        BasicDBObject dbQuery = new BasicDBObject();
        BasicDBObject field = new BasicDBObject();
        DBCursor cursor = db.find(dbQuery, collectionName);

        ArrayList<String> brandNames = new ArrayList<>();
        ArrayList<String> duplicates = new ArrayList<>();

        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            brandNames.add(obj.getString("name"));
        }



        cursor = db.find(dbQuery, collectionName);
        for (int i = 0; i < brandNames.size(); i++) {
            if (!cursor.hasNext()) {
                break;
            }
            BasicDBObject obj = (BasicDBObject) cursor.next();
            //                System.out.println("Mother Brand:" + brandNames.get(i));
            String[] split = brandNames.get(i).split(" ");
            int count = 0;
            duplicates.clear();
            duplicates.add(brandNames.get(i));
            for (int j = 0; j < brandNames.size(); j++) {
                if (i == j) {
                    continue;
                } else {
                    if (brandNames.get(j).contains(split[0])) {
                        count++;
                        duplicates.add(brandNames.get(j));
                    }
                }
            }

            if (count >= 1) {
                System.out.println(brandNames.get(i) + " was found in db " + (count + 1) + " times");
                //default icon, custom title
                Object[] possibilities = duplicates.toArray();
                String s = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select the best representation of the brand " + brandNames.get(i) + ":\n",
                        "Remove all other brands",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        possibilities,
                        "ham");

                System.out.println("Selected:" + s);
                duplicates.remove(s);

                if (s == null) {
                    System.out.println("Continuing without removing anything...");
                    continue;
                } else {
                    db.updateBrandsWithOne(duplicates, obj, collectionName);
                }
            }
        }



//        cursorDuplicates.




        frame.dispose();
    }

    private static ArrayList<String> printAllBrandNames() {
        ArrayList<String> returnList = new ArrayList();
        BasicDBObject obj = new BasicDBObject();
        MongoDB db = new MongoDB();
        DBCursor brandNames = db.find(obj, "brands");

        while (brandNames.hasNext()) {
            returnList.add((String) brandNames.next().get("name"));
        }
        return returnList;

    }

    public static void testStreaming() {

        db = new MongoDB();
        //find brand names
        BasicDBObject dbQuery = new BasicDBObject();
        BasicDBObject field = new BasicDBObject();
        field.put("name", 1);
        DBCursor cursor = db.findWithField(dbQuery, field, "brands");

        final ArrayList<String> brandNames = new ArrayList<>();



        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            brandNames.add(obj.getString("name").toLowerCase());
        }

        final int brandNamesSize = brandNames.size();
//        final HashSet<String> set = new HashSet(brandNames);

        String keywords[] = (String[]) brandNames.toArray(new String[brandNames.size()]);

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                String lang = status.getUser().getLang();
                if (lang != null && lang.equals("en")) {

                    System.out.println(status.getUser().getName() + " : " + status.getText() + ", lang :" + lang);
//                            + ",location: " + status.getUser().getLocation());
//                    String[] words = status.getText().split(" ");

                    for (int i = 0; i < brandNamesSize; i++) {
                        if (status.getText().contains(" " + brandNames.get(i).toLowerCase() + " ")) {
                            System.out.println(brandNames.get(i) + "\n#########");
                            break;
                        }
                    }
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.

        twitterStream.sample();


//        String[] language = {"en"};
//        FilterQuery fq = new FilterQuery();
//        fq.language(language);
//        twitterStream.filter(fq);
    }

    public static String readFile(String filename) {
        String content = null;
        File file = new File(filename); //for ex foo.txt
        try {
            FileReader reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void removeAllBrandsFromFile(String filename) {
        ArrayList<String> printAllBrandNames = printAllBrandNames();
        String tweetsTrainSet = readFile(filename).toLowerCase();

        for (int i = 0; i < printAllBrandNames.size(); i++) {
            Collections.sort(printAllBrandNames);
            System.out.println(printAllBrandNames.get(i));
            tweetsTrainSet = tweetsTrainSet.replaceAll(printAllBrandNames.get(i).toLowerCase(), " ");
        }

        System.out.println(tweetsTrainSet);
    }

    public void getFollowerIDs() {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            long cursor = -1;
            IDs ids;
            String screenName = "AlarV";
            System.out.println("Listing followers's ids.");
            do {
                ids = twitter.getFollowersIDs(screenName, cursor);



                for (long id : ids.getIDs()) {
                    System.out.println(id);
                }
            } while ((cursor = ids.getNextCursor()) != 0);
            System.out.println(ids.getIDs().length + " followers");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get followers' ids: " + te.getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        testStreaming();
    }
}
