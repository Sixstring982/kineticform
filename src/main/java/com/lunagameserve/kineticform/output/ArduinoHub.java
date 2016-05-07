package com.lunagameserve.kineticform.output;

import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;

import java.io.IOException;

/**
 * Created by sixstring982 on 5/5/16.
 */
public interface ArduinoHub {
    void connect();

    void update(TwitterCommandQueue queue) throws IOException;

    void stop();
}
