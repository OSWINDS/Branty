/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import static com.kallisto.SWN3.coreNLPpos;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author alarv
 */
public class FrequentWordsFeature {

    public static void main(String args[]) {
        MongoDB db = new MongoDB();
        BasicDBObject query = new BasicDBObject("tweets", new BasicDBObject("$exists", true)).append("category", "Fashion");
        BasicDBObject field = new BasicDBObject("tweets", 1);
        DBCursor cursor = db.findWithField(query, field, "brands");

        HashMap<String, Integer> lexicon = new HashMap();
        ValueComparator bvc = new ValueComparator(lexicon);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);

        while (cursor.hasNext()) {

            BasicDBList tweets = (BasicDBList) cursor.next().get("tweets");
            for (int i = 0; i < tweets.size(); i++) {
                BasicDBObject tweet = (BasicDBObject) tweets.get(i);
                BasicDBList hashtags = (BasicDBList) ((BasicDBObject) tweet.get("entities")).get("hashtags");
                String hashtagsString = "";
                if (hashtags == null || hashtags.isEmpty()) {
                    continue;

                } else {
                    for (int hashCounter = 0; hashCounter < hashtags.size(); hashCounter++) {
                        hashtagsString += ((BasicDBObject) hashtags.get(hashCounter)).getString("text")+" ";
                    }
                }
                //from twitter text
                //String preprocessedText = Preprocess.preprocessForSentiWordNet(tweet.getString("text"));
                //from hashtags, which seems better
                String preprocessedText = Preprocess.preprocessForSentiWordNet(hashtagsString);
                String[] split = preprocessedText.split(" ");
                for (int j = 0; j < split.length; j++) {
                    if (lexicon.containsKey(split[j])) {
                        lexicon.put(split[j], lexicon.get(split[j]) + 1);
                    } else {
                        lexicon.put(split[j], 0);
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
                continue;
            } else if (wordPOS.substring(0, 2).equals("NN")) {//noun
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
            } else {
                continue;
            }
            it.remove(); // avoids a ConcurrentModificationException
        }




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
