package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
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
public class SearchAPI {
    
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

    public static int[] getOwnTweets(String profileName) {
        int[] ret_fav = new int[3];
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            int count = 1, tweets_count = 0;
            Paging paging = new Paging(count, 200);
            List<Status> statuses = twitter.getUserTimeline(profileName, paging);
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
            }
            Thread.sleep(60000);
                //count++;
            //paging = new Paging(count, 200);
            //statuses = twitter.getUserTimeline(profileName, paging);
            //tweet_day = new LocalDate(statuses.get(0).getCreatedAt());
            //}
            ret_fav[2] = tweets_count;
        } catch (TwitterException | InterruptedException ex) {
            Logger.getLogger(SearchAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret_fav;
    }
    
    public HashMap<String, Object> getSearchAPIFeatures(DBCursor cursor){
        int counter = 0;
        BasicDBObject query = new BasicDBObject();
        String[] brandArray = new String[cursor.size()];
        int[] ret_fav = new int[3];
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            brandArray[counter] = ((String) ((BasicDBList) next.get("profile")).get(0)).replaceAll("@", "");
            counter++;
        }
        counter = 0;
        counter = numDiv(brandArray.length, 100);
        for (int i = 0; i < counter; i++) {
            try {
                String[] copyOfRange = Arrays.copyOfRange(brandArray, i * 100, i * 100 + 100);
                System.out.println("length:" + copyOfRange.length + " and i is:" + i);
                Twitter twitter = new TwitterFactory().getInstance();
                ResponseList<User> lookupUsers = twitter.lookupUsers(copyOfRange);
                for (User user : lookupUsers) {
                    if (!(user.getScreenName()).equals("null")) {
                        long duration = twitterHumanFriendlyDate(user.getCreatedAt().toString());
                        float portion = (float) (1.0 * user.getStatusesCount() / duration);
                        float followers_friend;
                        if (user.getFriendsCount() == 0) {
                            followers_friend = (float) (user.getFollowersCount());
                        } else {
                            followers_friend = (float) (1.0 * user.getFollowersCount() / (1.0 * user.getFriendsCount()));
                        }
                        System.out.println("---------------------------------------------------------------------");
                        System.out.println("ID:" + user.getId());
                        System.out.println("@" + user.getScreenName());
                        System.out.println("Followers = " + user.getFollowersCount());
                        System.out.println("Friends = " + user.getFriendsCount());
                        System.out.println("Followers/Friends = " + followers_friend);
                        System.out.println("Verified = " + user.isVerified());
                        System.out.println("Tweets of Brand = " + user.getStatusesCount());
                        System.out.println("On list = " + user.getListedCount());
                        System.out.println("Created since = " + duration + " months");
                        System.out.println("Tweets per month = " + portion);
                        ret_fav = getOwnTweets(user.getScreenName());
                        System.out.println("retweet number = " + ret_fav[0]);
                        System.out.println("favorite number = " + ret_fav[1]);
                        System.out.println("posted tweets = " + ret_fav[2]);
                        
                        
                        
                    }
                }
                Thread.sleep(60000);
            } catch (TwitterException | InterruptedException ex) {
                Logger.getLogger(SearchAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
