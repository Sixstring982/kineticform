package com.lunagameserve.kineticform.twitter;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by sixstring982 on 4/2/16.
 */
public class TwitterCommandQueue {
    private LinkedBlockingDeque<TwitterCommand> commands = new LinkedBlockingDeque<TwitterCommand>();
    public boolean isCommandAvailable() {
        return !commands.isEmpty();
    }

    public void insert(TwitterCommand command) {
        this.commands.add(command);
    }

    public TwitterCommand nextCommand() {
        if (!isCommandAvailable()) {
            return null;
        } else {
            try {
                return commands.take();
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    public int size() {
        return commands.size();
    }
}
