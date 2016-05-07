package com.lunagameserve.kineticform.twitter;

import com.lunagameserve.kineticform.output.Log;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sixstring982 on 4/2/16.
 */
public class TweetScanner {
    private static final int MAX_REMEMBERED_TWEETS = 50;
    private Thread thread;
    private AtomicBoolean running = new AtomicBoolean(false);
    private LinkedBlockingDeque<TwitterCommand> commands = new LinkedBlockingDeque<TwitterCommand>();
    public void start() {
        if (thread != null) {
            throw new IllegalStateException("TweetScanner already started.");
        }
        running.set(true);
        thread = new Thread(scanLoop);
        thread.start();
    }

    public void stop() {
        running.set(false);
        try {
            thread.join();
            thread = null;
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public TwitterCommand getNextCommand() {
        return commands.poll();
    }

    private TwitterFactory createFactory() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/twittercreds.txt")));

        try {
            cb.setOAuthConsumerKey(reader.readLine().trim());
            cb.setOAuthConsumerSecret(reader.readLine().trim());
            cb.setOAuthAccessToken(reader.readLine().trim());
            cb.setOAuthAccessTokenSecret(reader.readLine().trim());
        } catch (IOException e) {
            Log.e("Can't load twitter configuration.");
            throw new RuntimeException(e);
        }
        return new TwitterFactory(cb.build());
    }

    private String buildQueryString() {
        StringBuilder sb = new StringBuilder();
        TwitterCommand[] cs = TwitterCommand.values();
        for (int i = 0; i < cs.length; i++) {
            sb.append("#").append(cs[i]);
            if (i < cs.length - 1) {
                sb.append(" OR ");
            }
        }
        return sb.toString();
    }

    private final Runnable scanLoop = new Runnable() {
        public void run() {
            Twitter twitter = createFactory().getInstance();
            String queryString = buildQueryString();
            Queue<String> recentTweets = new ArrayDeque<String>();
            Set<String> currentTweets;
            boolean firstRound = true;
            while (running.get()) {
                Query query = new Query(queryString);
                query.setResultType(Query.ResultType.recent);
                try {
                    QueryResult result = twitter.search(query);
                    currentTweets = new HashSet<String>();
                    for (Status status : result.getTweets()) {
                        currentTweets.add("[" + status.getCreatedAt() + "] " + status.getText());
                    }

                    /* Which ones are brand new? */
                    currentTweets.removeAll(recentTweets);
                    for (String newTweet : currentTweets) {
                        Log.i("New tweet: \"" + newTweet + '"');
                        for (TwitterCommand c : TwitterCommand.values()) {
                            if (c.isInvokedBy(newTweet)) {
                                if (!firstRound) {
                                    commands.add(c);
                                }
                            }
                        }
                    }

                    recentTweets.addAll(currentTweets);
                    while (recentTweets.size() > MAX_REMEMBERED_TWEETS) {
                        recentTweets.remove();
                    }

                    firstRound = false;
                } catch (TwitterException e) {
                    Log.e("Twitter query failed:");
                    e.printStackTrace();
                    return;
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.e("TweetScanner interrupted.");
                    return;
                }
            }
        }
    };
}
