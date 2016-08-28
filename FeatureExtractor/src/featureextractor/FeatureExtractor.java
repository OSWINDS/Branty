/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import static featureextractor.SearchAPI.getOwnTweets;
import static featureextractor.SearchAPI.twitterHumanFriendlyDate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 *
 * @author alarv
 */
public class FeatureExtractor {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static String currentimgurl = "";

    private static int[] getSentiment(DBObject next) {

        int poscount = 0, negcount = 0, neutcount = 0;
        int[] sentiments = new int[3];
        Arrays.fill(sentiments, 0);
        BasicDBList tweets = (BasicDBList) next.get("tweets");
        if (tweets == null) {
            return sentiments;
        }
        for (int j = 0; j < tweets.size(); j++) {
            BasicDBObject tweet = (BasicDBObject) tweets.get(j);

            if (skipDate(tweet)) {
                continue;
            }

            String sentiment = (String) tweet.get("sentiment");

            switch (sentiment) {
                case "positive":
                    poscount++;
                    break;
                case "negative":
                    negcount++;
                    break;
                case "neutral":
                    neutcount++;
                    break;
            }
        }
        sentiments[0] = poscount;
        sentiments[1] = negcount;
        sentiments[2] = neutcount;
        return sentiments;

    }

    private static int getPositiveOnTrends(DBObject next, Object[] array) {
        BasicDBList tweets = (BasicDBList) next.get("tweets");
        int count = 0;

        if (tweets == null) {
            return 0;
        } else {
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);

                if (skipDate(tweet)) {
                    continue;
                }

                for (Object trendingWord : array) {
                    if (tweet.getString("text").toLowerCase().contains(trendingWord.toString().toLowerCase()) && tweet.getString("sentiment").equals("positive")) {
                        count++;
                    }
                }
            }
            return count;
        }
    }

    private static int getTweetCount(DBObject next) {
        BasicDBList tweets = (BasicDBList) next.get("tweets");
        int count = 0;
        if (tweets == null) {
            return 0;
        } else {
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);
                if (skipDate(tweet)) {
                    continue;
                }
                count++;
            }
            return count;
        }
    }

    //apotuxia gnk
    private static int getURLMentionCount(DBObject next) {
        int count = 0;
        BasicDBList urlsBig = (BasicDBList) next.get("url");
        BasicDBList urls = new BasicDBList();

        //teleiws pontio
        if (urlsBig.size() == 2) {
            BasicDBObject url = (BasicDBObject) urlsBig.get(0);
            urls.add((String) url.get("shortened"));
            url = (BasicDBObject) urlsBig.get(1);
            urls.add((String) url.get("expanded"));
        } else {
            BasicDBObject url = (BasicDBObject) urlsBig.get(0);
            urls.add(url.getString("expanded"));
        }

        BasicDBList tweets = (BasicDBList) next.get("tweets");
        if (tweets == null) {
            return 0;
        } else {
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);

                if (skipDate(tweet)) {
                    continue;
                }

                BasicDBList urlsFromTweet = (BasicDBList) tweet.get("urls");
//                    System.out.println(Arrays.toString(urlsArray));
                if (urlsFromTweet == null) {
                    continue;
                }
                urlsFromTweet.retainAll(urls);
                count += urlsFromTweet.size();
            }

            return count;
        }
    }

    private static int getMentionCount(DBObject next) {
        int count = 0;
        BasicDBList profiles = (BasicDBList) next.get("profile");

        for (int i = 0; i < profiles.size(); i++) {
            profiles.set(i, ((String) profiles.get(i)).replaceAll("@", ""));
        }

        BasicDBList tweets = (BasicDBList) next.get("tweets");
        if (tweets == null) {
            return 0;
        } else {
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);

                if (skipDate(tweet)) {
                    continue;
                }

                BasicDBList mentions = (BasicDBList) tweet.get("mentions");
//                    System.out.println(Arrays.toString(urlsArray));
                if (mentions == null) {
                    continue;
                }
                mentions.retainAll(profiles);
                count += profiles.size();
                //retainAll returns to profiles the intersection of profiles and mentions
            }

            return count;
        }
    }

    private static double getKloutScore(DBObject next) {
        try {
            //        long[] allIds = db.getAllIds("final_brands");
            String kloutKey = "gxbgfm2dpd4z6684pjkxaek3";

            //        for (int i = 0; i < allIds.length; i++) {
            String url = "http://api.klout.com/v2/user.json/";
            url += next.get("klout_id");
            url += "/";
            url += "score";
            url += "?";
            url += "key=";
            url += kloutKey;

            String json = sendGet(url);
            if (json == null) {
                return 0.0;
            }
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(json);
            double score = (double) object.get("score");
            return score;
//                db.insertFieldInto(new BasicDBObject("user_id", allIds[i]), new BasicDBObject("klout_id", kloutID), "test");
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;

    }

    private static String sendGet(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("X-Originating-Ip", "79.107.243.18");

            int responseCode = con.getResponseCode();
//            System.out.println("\nSending 'GET' request to URL : " + url);
//            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static HashMap<String, Object> getSearchAPI(String profileName) {
        try {
            int[] ret_fav = new int[3];
            HashMap<String, Object> searchAPIFeatures = new HashMap<>();
            Twitter twitter = new TwitterFactory().getInstance();
            int count = 1, tweets_count = 0;
            Paging paging = new Paging(count, 200);
            List<Status> statuses = twitter.getUserTimeline(profileName, paging);
            if(statuses == null || statuses.isEmpty() || statuses.get(0) == null || statuses.get(0).getCreatedAt() == null){
                return null;
            }
            
            LocalDate now = new LocalDate();
            LocalDate tweet_day = new LocalDate(statuses.get(0).getCreatedAt());
            LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
            //while (!tweet_day.isBefore(monday)) {
            for (Status status : statuses) {
                tweet_day = new LocalDate(status.getCreatedAt());
                if (tweet_day.isBefore(monday)) {
                    break;
                }
                if (!status.isRetweet() && !tweet_day.isBefore(monday)) {
                    ret_fav[0] += status.getRetweetCount();//Retweet Count
                    ret_fav[1] += status.getFavoriteCount();//Favorite Count
                    tweets_count++;
                }
            }   //count++;
            //paging = new Paging(count, 200);
            //statuses = twitter.getUserTimeline(profileName, paging);
            //tweet_day = new LocalDate(statuses.get(0).getCreatedAt());
            //}
            ret_fav[2] = tweets_count;
            User user = statuses.get(0).getUser();
            currentimgurl = user.getProfileImageURL();
            long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
            float portion = (float) (1.0 * user.getStatusesCount() / duration);
            float followers_friend;
            if (user.getFriendsCount() == 0) {
                followers_friend = (float) (user.getFollowersCount());
            } else {
                followers_friend = (float) (1.0 * user.getFollowersCount() / (1.0 * user.getFriendsCount()));
            }
            searchAPIFeatures.put("followers", user.getFollowersCount());
            searchAPIFeatures.put("followers_div_friends", followers_friend);
            searchAPIFeatures.put("verified", user.isVerified());
            searchAPIFeatures.put("listed", user.getListedCount());
            searchAPIFeatures.put("tweets_per_month", portion);
            searchAPIFeatures.put("retweet_count", ret_fav[0]);
            searchAPIFeatures.put("favorite_count", ret_fav[1]);
            searchAPIFeatures.put("last_week_tweet_count", ret_fav[2]);
//            System.out.println("Friends = " + user.getFriendsCount());
//            System.out.println("Followers/Friends = " + followers_friend);
//            System.out.println("Verified = " + user.isVerified());
//            System.out.println("Tweets of Brand = " + user.getStatusesCount());
//            System.out.println("On list = " + user.getListedCount());
//            System.out.println("Created since = " + duration + " months");
//            System.out.println("Tweets per month = " + portion);
//            System.out.println("retweet number = " + ret_fav[0]);
//            System.out.println("favorite number = " + ret_fav[1]);
//            System.out.println("posted tweets = " + ret_fav[2]);
            return searchAPIFeatures;
        } catch (TwitterException ex) {
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB("alarv", "220757");
        MongoDB dbFeatures = new MongoDB("alarv", "220757", "features");
        while (true) {
            DBObject findOne = db.findOne(new BasicDBObject("_id", "config_trends"), "config");
//            System.out.println(findOne);
            BasicDBList technologyList = (BasicDBList) findOne.get("Technology");
            Object[] technologyTrends = technologyList.toArray();

            BasicDBList fashionList = (BasicDBList) findOne.get("Fashion");
            Object[] fashionTrends = fashionList.toArray();

            BasicDBList foodbeveragesList = (BasicDBList) findOne.get("Food/Beverages");
            Object[] foodbeveragesTrends = foodbeveragesList.toArray();

            BasicDBList autoList = (BasicDBList) findOne.get("Auto");
            Object[] autoTrends = autoList.toArray();

            DBCursor cursor = db.find(new BasicDBObject(), "final_brands");
            cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
            while (cursor.hasNext()) {
                long start = System.nanoTime();
                BasicDBObject next = (BasicDBObject) cursor.next();

                BasicDBObject features = new BasicDBObject();
                LocalDate now = new LocalDate();
                features.append("date", now.toString());

                //sentiment
                int[] sentiments = getSentiment(next);
                features.append("positive", sentiments[0]);
                features.append("negative", sentiments[1]);
                features.append("neutral", sentiments[2]);

                //positive on trends
                String category = (String) next.get("category");
                Object[] properArray = null;

                switch (category) {
                    case "Technology":
                        properArray = technologyTrends;
                        break;
                    case "Fashion":
                        properArray = fashionTrends;
                        break;
                    case "Food/Beverages":
                        properArray = foodbeveragesTrends;
                        break;
                    case "Auto":
                        properArray = autoTrends;
                        break;
                }

                int positiveOnTrends = getPositiveOnTrends(next, properArray);
                features.append("positive_trending", positiveOnTrends);

                //tweet count
                int tweetCount = getTweetCount(next);
                features.append("tweet_count", tweetCount);

                //URL mention count
                int urlCount = getURLMentionCount(next);
                features.append("URL_mention_count", urlCount);

                //mention count
                int mentionCount = getMentionCount(next);
                features.append("mention_count", mentionCount);

                //klout score
                double kloutScore = getKloutScore(next);
                features.append("klout_score", kloutScore);

                //Search API
                HashMap<String, Object> ownTweets = getSearchAPI((String) ((BasicDBList) next.get("profile")).get(0));
                if (ownTweets != null) {
                    for (Map.Entry<String, Object> entry : ownTweets.entrySet()) {
                        features.append(entry.getKey(), entry.getValue());
                    }
                }

                System.out.println(next.get("name"));
                System.out.println(features);

                db.insertFieldInto(new BasicDBObject("_id",next.get("_id")), new BasicDBObject("imgurl",currentimgurl), "features");
                dbFeatures.updateFeatures((String) next.get("name"), features);

                long elapsedTimeMillis = System.nanoTime() - start;
                float elapsedTimeSec = elapsedTimeMillis / 1000000;
                if (60000 - elapsedTimeSec > 0) {
                    try {
                        //query at least every 60 seconds
                        Thread.sleep((long) (60000 - elapsedTimeSec));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
//            System.out.println("Key = " + entry.getKey() + ", Value = " +entry.getValue());
//        }
//        
//        //sentiment
//        HashMap<String, int[]> sentimentMap = getSentiment(cursor.copy());
//        for (Map.Entry<String, int[]> entry : sentimentMap.entrySet()) {
//            int[] sentiments = entry.getValue();
//            db.updateFeatures(entry.getKey(),new BasicDBObject("positive",sentiments[0]));
//            db.updateFeatures(entry.getKey(),new BasicDBObject("negative",sentiments[1]));
//            db.updateFeatures(entry.getKey(),new BasicDBObject("neutral",sentiments[2]));
////            System.out.println("Key = " + entry.getKey() + ", Value = " + Arrays.toString(entry.getValue()));
//        }
//        //most frequent words
////        TreeMap<String, Integer> trendingHashtags = getTrendingHashtags(cursor.copy(), "Food/Beverages");
//
//        //tweet count
//        HashMap<String, Integer> tweetMap = getTweetCount(cursor.copy());
//        for (Map.Entry<String, Integer> entry : tweetMap.entrySet()) {
////            System.out.println("Key = " + entry.getKey() + ", Value = " +entry.getValue());
//            db.updateFeatures(entry.getKey(),new BasicDBObject("tweet_count",entry.getValue()));
//        }
//        
//        //url mention count
//        HashMap<String, Integer> URLmap = getURLMentionCount(cursor.copy());
//        for (Map.Entry<String, Integer> entry : URLmap.entrySet()) {
//            db.updateFeatures(entry.getKey(),new BasicDBObject("URL_mention_count",entry.getValue()));
////            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
//        
//        //mention count
//        HashMap<String, Integer> mentionMap = getMentionCount(cursor.copy());
//        for (Map.Entry<String, Integer> entry : mentionMap.entrySet()) {
//            db.updateFeatures(entry.getKey(),new BasicDBObject("mention_count",entry.getValue()));
////            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
        //klout score
//        HashMap<String, Double> kloutScores = getKlout(cursor.copy());
//        for (Map.Entry<String, Double> entry : kloutScores.entrySet()) {
////            db.updateFeatures(entry.getKey(),new BasicDBObject("mention_count",entry.getValue()));
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
    }

    private static boolean skipDate(BasicDBObject tweet) {
        Date tweetDate = tweet.getDate("created_at");
        LocalDate now = new LocalDate();
        LocalDate tweet_day = new LocalDate(tweetDate);
        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        //while (!tweet_day.isBefore(monday)) {
        tweet_day = new LocalDate(tweetDate);
        return tweet_day.isBefore(monday);
    }

    static class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.    
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

}
