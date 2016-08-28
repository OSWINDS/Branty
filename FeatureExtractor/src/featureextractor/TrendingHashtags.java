/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextractor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 *
 * @author alarv
 */
public class TrendingHashtags {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MongoDB db = new MongoDB("alarv", "220757");
        TreeMap<String, Integer> sorted_map;
        try (DBCursor cursor = db.find(new BasicDBObject(), "final_brands")) {
            String category = "Food/Beverages";
            //Food/Beverages
            //Technology
            //Fashion
            //Auto
            HashMap<String, Integer> lexicon = new HashMap();
            FeatureExtractor.ValueComparator bvc = new FeatureExtractor.ValueComparator(lexicon);
            sorted_map = new TreeMap<>(bvc);
            while (cursor.hasNext()) {
                BasicDBObject next = (BasicDBObject) cursor.next();
//                System.out.println(next);
                String currentCategory = next.getString("category");
                if (!currentCategory.equals(category)) {
                    continue;
                }
                BasicDBList tweets = (BasicDBList) next.get("tweets");
                if (tweets == null) {
                    continue;
                }
                for (int j = 0; j < tweets.size(); j++) {
                    BasicDBObject tweet = (BasicDBObject) tweets.get(j);
                    LocalDate now = new LocalDate();
                    LocalDate tweet_day = new LocalDate(tweet.getDate("created_at"));
                    LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
//                System.out.println("tweet day:"+tweet_day);
//                System.out.println("monday:"+monday);
                    if (tweet_day.isBefore(monday)) {
                        continue;
                    }
                    BasicDBList hashtags = (BasicDBList) (tweet.get("hashtags"));
                    String hashtagsString = "";
                    if (hashtags == null || hashtags.isEmpty()) {
                        continue;
                    } else {
                        for (int hashCounter = 0; hashCounter < hashtags.size(); hashCounter++) {
                            hashtagsString += hashtags.get(hashCounter) + " ";
                        }
                    }
                    //from twitter text
                    //String preprocessedText = Preprocess.preprocessForSentiWordNet(tweet.getString("text"));
                    //from hashtags, which seems better
                    String preprocessedText = Preprocess.preprocessForSentiWordNet(hashtagsString);
                    String[] split = preprocessedText.split(" ");
                    for (String splitword : split) {
                        if (lexicon.containsKey(splitword)) {
                            lexicon.put(splitword, lexicon.get(splitword) + 1);
                        } else {
                            lexicon.put(splitword, 0);
                        }
                    }
                }
            }
            
            sorted_map.putAll(lexicon);
            SWN3 swn = new SWN3();
            Iterator it = sorted_map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (pairs.getKey().toString().length() <= 2) {
                    continue;
                }

                String wordPOS = swn.coreNLPpos(pairs.getKey().toString());
                if (wordPOS == null) {
                    sorted_map.remove((String) pairs.getKey());
                    continue;
                } else if (wordPOS.substring(0, 2).equals("NN")) {//noun
                    System.out.println((String) pairs.getKey() + " = " + pairs.getValue());
                } else {
                    sorted_map.remove((String) pairs.getKey());
                    continue;
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        for (Map.Entry entry : sorted_map.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }

    }

}
