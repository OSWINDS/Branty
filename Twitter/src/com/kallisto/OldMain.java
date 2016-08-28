/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.util.JSON;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author Alex
 */
public class OldMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        MongoDB db = new MongoDB();
        APIrequests api = new APIrequests();
        while (true) {
            //find brand names
            BasicDBObject dbQuery = new BasicDBObject();
            BasicDBObject field = new BasicDBObject("name", 1);
            DBCursor cursor = db.findWithField(dbQuery, field, "brands");

            ArrayList<String> brandNames = new ArrayList<>();

            while (cursor.hasNext()) {
                BasicDBObject obj = (BasicDBObject) cursor.next();
                brandNames.add(obj.getString("name"));
            }
            //shuffle brand names so that different brands are checked on every cycle
            Collections.shuffle(brandNames, new Random(System.nanoTime()));
            //end of brand names


            for (int brandCount = 0; brandCount < brandNames.size(); brandCount++) {
                long start = System.nanoTime();
                //pare ana 60" 100 tweets me auta ta keywords
                int count = 0;
//                    Twitter twitter = TwitterFactory.getSingleton();


//                Query query = new Query("traffic stuck OR road OR transportation OR street OR trafficlight OR ringroad -internet ");
                //building Query to include all @mentions and the name of the brand first with OR
                BasicDBList profiles = (BasicDBList) db.findOne(
                        new BasicDBObject("name", brandNames.get(brandCount)), "brands").get("profile");
                String buildQuery = brandNames.get(brandCount);
                for (int a = 0; a < profiles.size(); a++) {
                    buildQuery += " OR " + profiles.get(a);
                }

                //add Brand URL at the end of the query
                BasicDBList urls = (BasicDBList) db.findOne(new BasicDBObject("name", brandNames.get(brandCount)), "brands").get("url");
                buildQuery += " OR " + ((BasicDBObject) urls.get(0)).getString("shortened");
                buildQuery += " OR " + ((BasicDBObject) urls.get(1)).getString("expanded");

                QueryResult result = api.getQuery(buildQuery);


                for (Status tweet : result.getTweets()) {
//                        String tweetText = tweet.getText();
                    String username = tweet.getUser().getScreenName();
                    if (!tweet.isRetweet() && !username.toLowerCase().contains(brandNames.get(brandCount).toLowerCase())) {
                        System.out.println("Tweet: @" + tweet.getUser().getScreenName() + ":" + tweet.getText());
                        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = formatter.format(tweet.getCreatedAt());
                        System.out.println("Posted at:" + date);
//                        mongoDB.insert(tweet.getUser().getScreenName(), tweet.getText(), date);
                        //update with tweet current brand i
//                            BasicDBObject obj = new BasicDBObject().append("user", username)
//                                    .append("tweet", tweetText)
//                                    .append("date", date);
//
//                            if (tweet.getUser().getLocation() != null && !tweet.getUser().getLocation().trim().equals("")) {
//                                obj.append("location", tweet.getUser().getLocation());
//                            }
//
//                            if (tweet.getGeoLocation() != null) {
//                                GeoLocation gl = tweet.getGeoLocation();
//                                BasicDBObject geoLocation = new BasicDBObject("lat", gl.getLatitude()).append("lng", gl.getLongitude());
//                                obj.append("geolocation", geoLocation);
//                            }
                        String json = DataObjectFactory.getRawJSON(tweet);
                        BasicDBObject dbObject = (BasicDBObject) JSON.parse(json);
//                            System.out.println(dbObject);

                        db.updateTweets(dbObject, brandNames.get(brandCount),"brands");

                        count++;
                    }
                }
//                ResponseList<Status> retweetsOfMe = twitter.getRetweetsOfMe();
//                for(int i = 0;i < retweetsOfMe.size();i++){
//                    System.out.println(retweetsOfMe.get(i).getText());
//                }


                long elapsedTimeMillis = System.nanoTime() - start;
                float elapsedTimeSec = elapsedTimeMillis / 1000000;
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                System.out.println("Time of last parsing:" + dateFormat.format(cal.getTime()));
                System.out.println("Time elapsed for last brand:" + elapsedTimeSec + "\"");
                System.out.println("Time remaining for next query:" + (60000 - elapsedTimeSec) + "\"");
                System.out.println("Last brand updated was:" + brandNames.get(brandCount));

                if (60000 - elapsedTimeSec > 0) {//query at least every 60 seconds
                    Thread.sleep((long) (60000 - elapsedTimeSec));
                }
            }
        }
    }
}
