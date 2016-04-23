package com.lunagameserve.kineticform.output;

import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sixstring982 on 4/23/16.
 */
public class ArduinoHub {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;
    private static final String[] PORT_NAMES = new String[] {
            "/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2"
    };

    /* A01234567
     *  89abcdef
     * A--------
     *  --------
     * A--------
     *  --------
     */

    private OutputStream[] outputs = new OutputStream[PORT_NAMES.length];

    public void connect() {
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = new FalseArduino(i);
        }
    }

    public void update(TwitterCommandQueue queue) throws IOException {
        if (queue.isCommandAvailable()) {
            sendCommand(queue.nextCommand());
        }
    }

    private void sendCommand(TwitterCommand command) throws IOException {
        boolean[] writtenTo = new boolean[outputs.length];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int delta = command.getPredicate().delta(x, y, WIDTH, HEIGHT);
                if (delta > 0) {
                    int outIdx = y / 2;
                    int idx = (y % 2) * WIDTH + x;

                    writtenTo[outIdx] = true;
                    outputs[outIdx].write(new byte[]{
                            (byte) idx,
                            (byte) delta
                    });
                }
            }
        }

        for (int i = 0; i < writtenTo.length; i++) {
            if (writtenTo[i]) {
                outputs[i].write(0xff);
            }
        }
    }

    public void stop() {
        for (OutputStream out : outputs) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
