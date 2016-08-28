package com.kallisto;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 *
 * @author tweety
 */
public class Tweety {

    private static MongoDB db;

    public static void sentimentAnalysis() {
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
            FileInputStream fstream = new FileInputStream("tweetsTrainingSet.txt");
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
                int n = JOptionPane.showOptionDialog(frame,
                        columnDetail[1] + "--> " + columnDetail[2] + " " + columnDetail[3] + " counter: " + counter,
                        "Branty",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]);
                if (n == 0) {
                    writeToFile("annaAnnotated.txt", "'" + StringEscapeUtils.escapeEcmaScript(columnDetail[2]) + "'" + "," + columnDetail[3] + "," + "positive");
                } else if (n == 1) {
                    writeToFile("annaAnnotated.txt", "'" + StringEscapeUtils.escapeEcmaScript(columnDetail[2]) + "'" + "," + columnDetail[3] + "," + "negative");
                } else if (n == 2) {
//                    writeToFile("annaAnnotated.txt", "'" + StringEscapeUtils.escapeEcmaScript(columnDetail[2]) + "'" + "," + columnDetail[3] + "," + "neutral");
                }
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
            System.err.println("Error: " + e.getMessage());
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
                    .getLogger(Tweety.class
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

    public static int numDiv(int num1, int num2) {
        if (num1 % num2 == 0) {
            num1 /= num2;
            return num1;
        } else {
            num1 /= num2;
            return num1 + 1;
        }
    }

    public static long twitterHumanFriendlyDate(String dateStr) {
        // parse Twitter date
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        Date created = null;
        try {
            created = dateFormat.parse(dateStr);
        } catch (Exception e) {
            return -1;
        }

        // today
        Date today = new Date();

        // how much time since (ms)
        Long duration = today.getTime() - created.getTime();

        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        long day = hour * 24;
        long months = (long) Math.ceil(duration / (day * 30));
        return months;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        db = new MongoDB(); 
         
        int counter = 0;
        String collectionName = "final_brands";
        BasicDBObject query = new BasicDBObject();
        DBCursor cursor = db.find(query, collectionName);
        HashMap<String, Integer> map = new HashMap();
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            String name = (String) ((BasicDBList) next.get("profile")).get(0);
            if (next.get("tweets") != null) {
                map.put(name, ((BasicDBList) next.get("tweets")).size());
            } else {
                map.put(name, 0);
            }
            //counter += ((BasicDBList) next.get("profile")).size();
            counter++;
        }
        String[] brandArray = new String[counter];
        counter = 0;
        for (Map.Entry pairs : map.entrySet()) {
            if (pairs.getKey().equals("")) {
                continue;
            } else {
//                for (int j = 0; j < ((BasicDBList)pairs.getKey()).size(); j++) {
//                    brandArray[counter] = ((String) ((BasicDBList)pairs.getKey()).get(j)).replaceAll("@", "");
//                    System.out.println("brandArray = " + brandArray[counter]);
//                    counter++;
//                }
                brandArray[counter] = ((String) pairs.getKey()).replaceAll("@", "");
                //System.out.println("brandArray = " + brandArray[counter]);
                counter++;
            }
        }
        counter = numDiv(brandArray.length, 100);
        System.out.println("length is " + brandArray.length);
        System.out.println("Counter is " + counter);
        for (int i = 0; i < counter; i++) {
            try {
                String[] copyOfRange = Arrays.copyOfRange(brandArray, i * 100, i * 100 + 100);
                System.out.println("length:" + copyOfRange.length + " and i is:" + i);
                Twitter twitter = new TwitterFactory().getInstance();
                ResponseList<User> lookupUsers = twitter.lookupUsers(copyOfRange);
                for (User user : lookupUsers) {
                    if (!(user.getScreenName()).equals("null") && user.getFollowersCount() > 50000&& user.getFollowersCount() < 100000) {
                        long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
                        float portion = (float) (1.0 * user.getStatusesCount() / duration);
                        Paging paging = new Paging(1, 1000);
//                        List<Status> statuses = twitter.getUserTimeline(user.getId(), paging);
                        List<String> list = new ArrayList();
                        list.add("@" + user.getScreenName());
                        BasicDBObject updateQuery = new BasicDBObject("profile", new BasicDBObject("$in", list));
                        System.out.println("---------------------------------------------------------------------");
                        System.out.println("ID:" + user.getId());
                        System.out.println("@" + user.getScreenName());
                        System.out.println("language:"+user.getLang());
                        System.out.println("Followers = " + user.getFollowersCount());
                        
//                        db.insertFieldInto(updateQuery, new BasicDBObject("user_id",user.getId()), "brands");

//                        System.out.println("Verified = " + (user.isVerified() ? 1 : 0));
//                        System.out.println("Tweets of Brand = " + user.getStatusesCount());
//                        System.out.println("On list = " + user.getListedCount());
//                        System.out.println("URL = " + user.getURL());
//                        writeToFile("urls.txt", user.getScreenName() + "\t" + user.getURL());
////                        for (Status status : statuses) {
////                            System.out.println(status.getUser().getId() +"\t" + user.getId());
////                            if (!status.isRetweet()) {
////                                System.out.println("Tweets Id = " + status.getText());
////                            }
////                        }
//
//                        System.out.println("Created since = " + duration + " months");
//                        System.out.println("Tweets per months = " + portion);
//                        System.out.println("Tweets for Brand = " + map.get("@" + user.getScreenName()));
                    }
                }
                Thread.sleep(60000);
            } catch (TwitterException ex) {
                Logger.getLogger(Tweety.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Tweety.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
