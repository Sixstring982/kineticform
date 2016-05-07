package com.lunagameserve.kineticform.drivers;

import com.lunagameserve.kineticform.output.*;
import com.lunagameserve.kineticform.twitter.TweetScanner;
import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by sixstring982 on 5/4/16.
 */
public class ArduinoDriver {

    public void run() throws IOException, InterruptedException {
        TwitterCommandQueue queue = new TwitterCommandQueue();
        ArduinoHub hub = new StdoutArduinoHub();
        TweetScanner scanner = new TweetScanner();
        Log.setLevel(LogLevel.INFO);
        Log.setTarget(new PrintStream("kinetic-form.log"));
        TwitterCommand command;
        boolean running = true;

        scanner.start();
        hub.connect();

        while (running) {
            if (System.in.available() > 0) {
                running = false;
            }
            Thread.sleep(100);

            command = scanner.getNextCommand();
            if (command != null) {
                queue.insert(command);
            }

            hub.update(queue);
        }

        hub.stop();
        scanner.stop();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new ArduinoDriver().run();
    }
}
