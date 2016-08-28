package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public void getOwnTweets(String profileName) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
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
        } catch (TwitterException | InterruptedException ex) {
            Logger.getLogger(Tweety.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB();

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
                    if (!(user.getScreenName()).equals("null") && user.getFollowersCount() > 50000 && user.getFollowersCount() < 100000) {
                        long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
                        float portion = (float) (1.0 * user.getStatusesCount() / duration);
                        Paging paging = new Paging(1, 200);
                        Thread.sleep(60000);
                        List<Status> statuses = twitter.getUserTimeline(user.getId(), paging);
//                        List<String> list = new ArrayList();
//                        list.add("@" + user.getScreenName());
                        System.out.println("---------------------------------------------------------------------");
                        System.out.println("ID:" + user.getId());
                        System.out.println("@" + user.getScreenName());
                        System.out.println("language:" + user.getLang());
                        System.out.println("Followers = " + user.getFollowersCount());

                        System.out.println("Verified = " + user.isVerified());
                        System.out.println("Tweets of Brand = " + user.getStatusesCount());
                        System.out.println("On list = " + user.getListedCount());
                        System.out.println("URL = " + user.getURL());
                        
                        
                        for (Status status : statuses) {
                            System.out.println(status.getUser().getId() + "\t" + user.getId());
                            if (!status.isRetweet()) {
                                System.out.println("Tweets Id = " + status.getText());
                            }
                        }

//
                        System.out.println("Created since = " + duration + " months");
                        System.out.println("Tweets per month = " + portion);
                        System.out.println("Tweets for Brand = " + map.get("@" + user.getScreenName()));
                    }
                }
                Thread.sleep(60000);
            } catch (TwitterException | InterruptedException ex) {
                Logger.getLogger(Tweety.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
