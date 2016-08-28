/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author alarv
 */
public class Brand {

    private final DBObject brand;
    private final MongoDB db;
    private final BasicDBList tweets;
    private static HashMap<Integer, Integer> testMap = new HashMap();

    /**
     * @param args the command line arguments
     */
    Brand(MongoDB db, String name) {
        this.db = db;
        brand = db.findOne(new BasicDBObject("name", name), "brands");
        tweets = (BasicDBList) brand.get("tweets");
    }

    public String[] getProfiles() {
        BasicDBList profileList = (BasicDBList) brand.get("profile");
        String[] profileNames = new String[profileList.size()];

        for (int i = 0; i < profileList.size(); i++) {
            profileNames[i] = ((String) profileList.get(i)).replaceAll("@", "");
        }

        return profileNames;
    }

    public double getMentionAvg() {
        DBObject currentBrand = brand;
        BasicDBList profiles = (BasicDBList) currentBrand.get("profile");

        if (tweets == null) {
            return 0.0;
        }
        int count = 0;
        for (int i = 0; i < tweets.size(); i++) {
            BasicDBObject tweet = (BasicDBObject) tweets.get(i);
            String text = tweet.getString("text");
            for (int j = 0; j < profiles.size(); j++) {
                if (text.contains((CharSequence) profiles.get(j))) {
                    System.out.println(text);
                    count++;
                    break;
                }
            }
        }
        return (double) 1.0 * count / tweets.size();

//        for (Map.Entry entry : map.entrySet()) {
//            System.out.println(entry.getKey() + ", " + entry.getValue());
//        }
    }

    public double getSentimentPosAvg() {
        if (tweets == null) {
            return 0.0;
        }
        int count = 0;
        for (int i = 0; i < tweets.size(); i++) {
            BasicDBObject tweet = (BasicDBObject) tweets.get(i);
            String sentiment = tweet.getString("sentiment");
            if (sentiment == null) {
                continue;
            }
            if (sentiment.equals("positive")) {
                count++;
            }
        }
        return (double) 1.0 * count / tweets.size();
    }

    public double getSentimentNegAvg() {
        if (tweets == null) {
            return 0.0;
        }
        int count = 0;
        for (int i = 0; i < tweets.size(); i++) {
            BasicDBObject tweet = (BasicDBObject) tweets.get(i);
            String sentiment = tweet.getString("sentiment");
            if (sentiment == null) {
                continue;
            }
            if (sentiment.equals("negative")) {
                count++;
            }
        }
        return (double) 1.0 * count / tweets.size();
    }

    public double getAuthority() {
        //Σ (number of retweets of tweet / followers)  * ( number of retweeted tweets / number of total tweets)
        double propagation = 0;
        int retweetedCount = 0;
        if (this.tweets == null) {
            return 0.0;
        }

        for (int i = 0; i < tweets.size(); i++) {
            BasicDBObject tweet = (BasicDBObject) tweets.get(i);
            int followers = ((BasicDBObject) tweet.get("user")).getInt("followers_count");
            if (followers < 10) {
                continue;
            }
            int retweetCount = tweet.getInt("retweet_count");

            propagation += (double) 1.0 * retweetCount / followers;
            System.out.println(retweetCount+","+followers);
            if (tweet.getInt("retweet_count") != 0) {
                retweetedCount++;
            }
        }
        return propagation * ((double) 1.0 * retweetedCount / tweets.size());
    }

    public String getName() {
        return (String) brand.get("name");
    }

    public static void main(String[] args) {
//        MongoDB db = new MongoDB();
//        TreeMap<Double, String> map = new TreeMap();
//        DBCursor cursor = db.findWithField(new BasicDBObject("category","Technology"), new BasicDBObject("name", 1), "brands");
//
//        while (cursor.hasNext()) {
//            Brand b = new Brand(db, (String) cursor.next().get("name"));
////            double authority = b.getAuthority();
////            System.out.println(b.getName() + ":" + b.getAuthority());
////            map.put(authority,b.getName());
//            
//        }
//          for (Map.Entry entry : map.entrySet()) {
//            System.out.println(entry.getKey() + ", " + entry.getValue());
//        }
//        APIrequests api = new APIrequests();
//        Brand b = new Brand(db, "adidas");
//        String [] profileNames = b.getProfiles();
//        for(int i = 0;i < profileNames.length;i++){
//            System.out.println(profileNames[i]);
//        }
//        
//        HashMap<String, Integer> followerCount = api.getFollowerCount(b.getProfiles());
//        
//        System.out.println(followerCount);
        MongoDB db = new MongoDB();
        Brand b = new Brand(db, "adidas");
        b.getAuthority();
    }
}
