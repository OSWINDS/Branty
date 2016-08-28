/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.cybozu.labs.langdetect.LangDetectException;
import static com.kallisto.Branty.statusesToInsert;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 *
 * @author alarv
 */
/*
 private List<Callable> tasks;

 public StoringThread() {
 this.tasks = Collections.synchronizedList(
 new ArrayList<Callable>());
 }

 public void addTask(Callable task) {
 this.tasks.add(task);
 }

 public void run() throws InterruptedException {
 long waitingInterval = 60000;//60 seconds
        
 while (true) {
 while (!tasks.isEmpty()) {
 Callable task = tasks.get(0);
 long start = System.nanoTime();
 //                task.run();
 tasks.remove(0);
 long elapsedTimeMillis = System.nanoTime() - start;
 float elapsedTimeSec = elapsedTimeMillis / 1000000;
 Thread.sleep((long) (waitingInterval - elapsedTimeSec));
 }
 System.out.println("API Pool is empty, waiting for "+(waitingInterval / 1000)+" seconds");
 Thread.sleep(waitingInterval);
 }
 }

 public static void main(String args[]) {
 Runnable task = new Runnable() {
 @Override
 public void run() {
 System.out.println(System.currentTimeMillis());
 }
 };
 Runnable task1 = new Runnable() {
 @Override
 public void run() {
 System.out.println("shah");
 }
 };
 Runnable task2 = new Runnable() {
 @Override
 public void run() {
 System.out.println("shahds");
 }
 };
 StoringThread test = new StoringThread();
 //        test.addTask(task);
 //        test.addTask(task1);
 //        test.addTask(task2);
 try {
 test.run();
 } catch (InterruptedException ex) {
 Logger.getLogger(StoringThread.class.getName()).log(Level.SEVERE, null, ex);
 }
 }*/
public class StoringThread extends Thread {

    
    private MongoDB db;
    private WEKA weka;
    private String collectionName;

    StoringThread() {
    }

    StoringThread(Runnable runnable, MongoDB db) throws LangDetectException {
        super(runnable);
        this.db = db;
        this.weka = new WEKA();
        this.collectionName = "final_brands";
    }


    @Override
    public void run() {
        int mb = 1024*1024;
        while (true) {
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory() / mb;
//            System.out.println("Free Memory:"+freeMemory);
            int usedMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / mb);
//            System.out.println("usedMemory:"+usedMemory);
            if(freeMemory < 500){
                statusesToInsert.clear();
            }
            if (!statusesToInsert.isEmpty()) {
                try {
                    Status status = statusesToInsert.take();

                    String brand = findMatchingBrand(status.getText());
                    if (brand != null) {
                        String sentiment = weka.getTweetSentiment(status.getText());

//                            System.out.println("-----------");
//                            System.out.println("ID:" + status.getId());
//                            System.out.println("Created At:" + status.getCreatedAt());
//                            System.out.println("@" + status.getUser().getScreenName());
//                            System.out.println("Text:" + status.getText());
//                            System.out.println("Hashtags:" + Arrays.toString(status.getHashtagEntities()));
//                            System.out.println("URLs:" + status.getURLEntities());
//                            System.out.println("Mentions:" + status.getUserMentionEntities());
//                            System.out.println("Retweet Count:" + status.getRetweetCount());
//                            System.out.println("Sentiment:" + sentiment);

                        BasicDBObject tweetToInsert = new BasicDBObject();
                        //general info
                        tweetToInsert.append("ID", status.getId());
                        tweetToInsert.append("created_at", status.getCreatedAt());
                        tweetToInsert.append("text", status.getText());
                        tweetToInsert.append("retweet_count", status.getRetweetCount());
                        tweetToInsert.append("favorite_count", status.getFavoriteCount());
                        if (status.getGeoLocation() != null) {
                            GeoLocation gl = status.getGeoLocation();
                            BasicDBObject geoLocation = new BasicDBObject();
                            geoLocation.append("lat", gl.getLatitude());
                            geoLocation.append("lng", gl.getLongitude());
                            
                            tweetToInsert.append("geolocation", geoLocation);
                        }else{
                            tweetToInsert.append("geolocation", null);
                        }
                        tweetToInsert.append("sentiment", sentiment);

                        //user
                        BasicDBObject userData = new BasicDBObject();
                        userData.append("user_id", status.getUser().getId());
                        userData.append("followers_count", status.getUser().getFollowersCount());
                        userData.append("friends_count", status.getUser().getFriendsCount());
                        userData.append("location", status.getUser().getLocation());
                        tweetToInsert.append("user", userData);

                        //hashtags
                        HashtagEntity[] hashtagEntities = status.getHashtagEntities();
                        if (hashtagEntities != null && hashtagEntities.length != 0) {
                            BasicDBList hashtags = new BasicDBList();
                            for (int i = 0; i < hashtagEntities.length; i++) {
                                hashtags.add(hashtagEntities[i].getText());
                            }
                            tweetToInsert.append("hashtags", hashtags);
                        }else{
                            tweetToInsert.append("hashtags", null);
                        }

                        //urls
                        URLEntity[] urlEntities = status.getURLEntities();
                        if (urlEntities != null && urlEntities.length != 0) {
                            BasicDBList urls = new BasicDBList();
                            for (int i = 0; i < urlEntities.length; i++) {
                                urls.add(urlEntities[i].getText());
                            }
                            tweetToInsert.append("urls", urls);
                        }else{
                            tweetToInsert.append("urls", null);
                        }

                        //mentions
                        UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
                        if (userMentionEntities != null && userMentionEntities.length != 0) {
                            BasicDBList mentions = new BasicDBList();
                            for (int i = 0; i < userMentionEntities.length; i++) {
                                mentions.add(userMentionEntities[i].getName());
                            }
                            tweetToInsert.append("mentions", mentions);
                        }else{
                            tweetToInsert.append("mentions",null);
                        }
                        
//                        System.out.println("#########");
//                        System.out.println("Brand:" + brand);
//                        System.out.println("tweet to insert:" + tweetToInsert);

                        db.updateTweets(tweetToInsert, brand, collectionName);

                    }
                } catch (LangDetectException ex) {
//                    Logger.getLogger(StoringThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(StoringThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private String findMatchingBrand(String text) {
        String[] split = text.trim().split(" ");
        for (String word : split) {
            if (Branty.brandNames.contains(word.toLowerCase())) {
                return word;
            }
        }

        return null;
    }
}
