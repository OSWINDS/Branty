/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author alarv
 */
public class APIrequests {

    private static Twitter twitter;
    private StoringThread scheduleThread; 

    public APIrequests() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        twitter = new TwitterFactory(cb.build()).getInstance();
//        test = new ScheduledThread();
    }

    public QueryResult getQuery(final String buildQuery) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    Query query = new Query(buildQuery);
                    //                GeoLocation centerOfAegean = new GeoLocation(38.891033, 23.946762);
                    //                query.geoCode(centerOfAegean, 500, "km");
                    query.setCount(10000);//8esame 10.000, alla pairnei mexri 100
                    query.setLang("en");
                    twitter.search(query);
                } catch (TwitterException ex) {
                    Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        
        return null;


    }

    public void getOwnTweets(String profileName) {


        Twitter twitter = new TwitterFactory().getInstance();
        try {
            int count = 1;
            Paging paging = new Paging(count, 200);
            List<Status> statuses = twitter.getUserTimeline(profileName, paging);

            while (!statuses.isEmpty()) {

                for (Status status : statuses) {
                    if (!status.isRetweet()) {
                        System.out.println("rt:" + status.getRetweetCount() + "\tfv:" + status.getFavoriteCount());
                    }
                }
                Thread.sleep(60000);
                count++;
                paging = new Paging(count, 200);
                statuses = twitter.getUserTimeline(profileName, paging);
                System.out.println("Current Page" + count);
            }

//                Thread.sleep(60000);
        } catch (InterruptedException ex) {
            Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TwitterException ex) {
            Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String, Integer> getFollowerCount(String[] profileNames) {
        HashMap<String, Integer> map = new HashMap();
        try {
            ResponseList<User> lookupUsers = twitter.lookupUsers(profileNames);
            for (User user : lookupUsers) {
                map.put(user.getScreenName(), user.getFollowersCount());
            }

            return map;
        } catch (TwitterException ex) {
            Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new HashMap();
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

    public ResponseList<User> getUsers(String[] brandArray) {
        int counter = numDiv(brandArray.length, 100);
        System.out.println("length is " + brandArray.length);
        System.out.println("Counter is " + counter);
        for (int i = 0; i < counter; i++) {
            try {
                String[] copyOfRange = Arrays.copyOfRange(brandArray, i * 100, i * 100 + 100);
                System.out.println("length:" + copyOfRange.length + " and i is:" + i);
                Twitter twitter = new TwitterFactory().getInstance();
                ResponseList<User> lookupUsers = twitter.lookupUsers(copyOfRange);
                for (User user : lookupUsers) {
                    if (!(user.getScreenName()).equals("null")) {
//                        long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
//                        float portion = (float) (1.0 * user.getStatusesCount() / duration);
//                        Paging paging = new Paging(1, 1000);
//                        List<Status> statuses = twitter.getUserTimeline(user.getId(), paging);

                        System.out.println("---------------------------------------------------------------------");
                        System.out.println("@" + user.getScreenName());
                        System.out.println("Followers = " + user.getFollowersCount());
                        System.out.println("Verified = " + (user.isVerified() ? 1 : 0));
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
                Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TwitterException {
        MongoDB db = new MongoDB();
        int counter = 0;
        BasicDBObject query = new BasicDBObject();
        DBCursor cursor = db.findWithField(query, new BasicDBObject("profile", 1), "brands");
        ArrayList<BasicDBList> listofprofiles = new ArrayList();

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            BasicDBList profile = ((BasicDBList) next.get("profile"));
//            if (profile.size() <= 1) {
//                continue;
//            }
            listofprofiles.add(profile);
            counter += ((BasicDBList) next.get("profile")).size();
        }
        String[] brandArray = new String[counter];
        counter = 0;
        for (int i = 0; i < listofprofiles.size(); i++) {
            for (int j = 0; j < listofprofiles.get(i).size(); j++) {
                brandArray[counter] = (String) listofprofiles.get(i).get(j).toString().replaceAll("@", "");
                counter++;
            }
        }

//        System.out.println(brandArray.length); System.exit(0);

        counter = 0;


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
                    if (!(user.getScreenName()).equals("null")) {
//                        long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
//                        float portion = (float) (1.0 * user.getStatusesCount() / duration);
//                        Paging paging = new Paging(1, 1000);
//                        List<Status> statuses = twitter.getUserTimeline(user.getId(), paging);

                        System.out.println("---------------------------------------------------------------------");
                        System.out.println("@" + user.getScreenName());
                        System.out.println("Followers = " + user.getFollowersCount());
                        System.out.println("Verified = " + (user.isVerified() ? 1 : 0));
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
                Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(APIrequests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
