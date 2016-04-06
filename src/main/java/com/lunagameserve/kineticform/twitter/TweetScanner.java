package com.lunagameserve.kineticform.twitter;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sixstring982 on 4/2/16.
 */
public class TweetScanner {
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
            System.err.println("Can't load twitter configuration.");
            throw new RuntimeException(e);
        }
        return new TwitterFactory(cb.build());
    }

    private final Runnable scanLoop = new Runnable() {
        public void run() {
            Twitter twitter = createFactory().getInstance();
            TwitterCommand[] comms = TwitterCommand.values();
            String[] initialTweets = new String[comms.length];
            for (int i = 0; i < initialTweets.length; i++) {
                initialTweets[i] = "";
            }
            int commandIndex = 0;
            while (running.get()) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.err.println("TweetScanner interrupted.");
                    return;
                }
                Query query = new Query("#" + comms[commandIndex].toString());
                try {
                    QueryResult result = twitter.search(query);
                    String newest = result.getTweets().get(0).getText();
                    if (!initialTweets[commandIndex].equals(newest)) {
                        /* New tweet! */
                        if (initialTweets[commandIndex].length() == 0) {
                            initialTweets[commandIndex] = newest;
                            System.out.println("New tweet: \"" + newest + '"');
                            continue;
                        }
                        System.out.println("New tweet: \"" + newest + '"');
                        commands.add(comms[commandIndex]);
                        initialTweets[commandIndex] = newest;
                    }
                    commandIndex = (commandIndex + 1) % comms.length;
                } catch (TwitterException e) {
                    System.err.println("Twitter query failed:");
                    e.printStackTrace();
                    return;
                }
            }
        }
    };
}
