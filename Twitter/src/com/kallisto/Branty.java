/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kallisto;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 *
 * @author alarv
 */
public final class Branty {

    private static StoringThread storingThread;
    static BlockingQueue<Status> statusesToInsert;
    private static int count = 0;
    static HashSet<String> brandNames;
    static final String collectionName = "final_brands";

    /**
     * Main entry of this application.
     *
     * @param args follow(comma separated user ids) track(comma separated filter
     * terms)
     * @throws twitter4j.TwitterException
     */
    public static void main(String[] args) throws TwitterException, LangDetectException {
        MongoDB db = new MongoDB("alarv", "220757");
        DetectorFactory.loadProfile("profiles");
//        final Detector detector = DetectorFactory.create();
        statusesToInsert = new LinkedBlockingQueue<>();
        String[] brandNamesArray = db.getAllNamesAndProfiles(collectionName);
        brandNames = new HashSet<String>();
        System.out.println("adding all brandnames to a hash set...");
        for (int i = 0; i < brandNamesArray.length; i++) {
            brandNames.add(brandNamesArray[i].toLowerCase());
        }
        System.out.println("done adding");

        Runnable runnable = new StoringThread();
        Runnable runnable2 = new StoringThread();
        Runnable runnable3 = new StoringThread();
        Runnable runnable4 = new StoringThread();
        storingThread = new StoringThread(runnable, db);
        storingThread.start();
        new StoringThread(runnable2, db).start();
        new StoringThread(runnable3, db).start();
        new StoringThread(runnable4, db).start();

        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                count++;

//                    detector.append(status.getText());
//                    String lang = detector.detect();
                //if (count % 10 == 0) 
                if (!status.isRetweet()) {
                    statusesToInsert.add(status);
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

        String collectionName = "final_brands";
        long[] followArray = db.getAllIds(collectionName);
        String[] trackArray = db.getAllNames(collectionName);
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.count(0);
        filterQuery.track(trackArray);
        String[] languages = new String[1];
        languages[0] = "en";
        filterQuery.language(languages);
        twitterStream.filter(filterQuery);
    }
}
