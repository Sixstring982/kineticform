package com.lunagameserve.kineticform.output;

import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sixstring982 on 5/7/16.
 */
public class StdoutArduinoHub implements ArduinoHub {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;

    @Override
    public void connect() {
        /* No need to connect */
    }

    @Override
    public void update(TwitterCommandQueue queue) throws IOException {
        if (queue.isCommandAvailable()) {
            TwitterCommand command = queue.nextCommand();
            OutputStream out = System.out;
            HashMap<Integer, List<Byte>> arduinos = new HashMap<>();
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    int delta = command.getPredicate().delta(x, y, WIDTH, HEIGHT);
                    if (delta > 0) {
                        int outIdx = y / 2;
                        int idx = (y % 2) * WIDTH + x;

                        if (!arduinos.containsKey(outIdx)) {
                            arduinos.put(outIdx, new ArrayList<Byte>());
                        }

                        List<Byte> bytes = arduinos.get(outIdx);

                        bytes.add((byte)idx);
                        bytes.add((byte)delta);
                    }
                }
            }

            for (Integer idx : arduinos.keySet()) {
                if (idx != 0) {
                    continue; /* KILL THIS WHEN WORKING WITH MORE ARDUINOS */
                }
                List<Byte> bytes = arduinos.get(idx);
                Byte[] byteArray = new Byte[bytes.size()];
                bytes.toArray(byteArray);

                out.write(new byte[] {
                        idx.byteValue(),
                        (byte) (bytes.size() + 1)
                });

                for (byte b : byteArray) {
                    out.write(b);
                }

                out.write(0xff);
            }
            out.flush();
        }
    }

    @Override
    public void stop() {
        /* No need to stop */
    }
}
