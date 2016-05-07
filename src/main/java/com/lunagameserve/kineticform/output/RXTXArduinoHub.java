package com.lunagameserve.kineticform.output;

import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Created by sixstring982 on 4/23/16.
 */
public class RXTXArduinoHub implements ArduinoHub {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;
    private static final String[] PORT_NAMES = new String[] {
            "/dev/ttyACM0"
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
        int ports = 0;
        for (String portName : PORT_NAMES) {
            System.setProperty("gnu.io.rxtx.SerialPorts", portName);
            CommPortIdentifier i;
            Enumeration e = CommPortIdentifier.getPortIdentifiers();
            while (e.hasMoreElements()) {
                i = (CommPortIdentifier)e.nextElement();
                if (i.getName().equals(portName)) {
                    try {
                        CommPort port = i.open("KineticForm", 1000);
                        outputs[ports++] = port.getOutputStream();
                    } catch (PortInUseException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        if (ports != PORT_NAMES.length) {
            throw new RuntimeException(
                    String.format(
                            "Could only open %d Arduinos!", ports
                    )
            );
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

                    if (outIdx < outputs.length) {
                        writtenTo[outIdx] = true;
                        outputs[outIdx].write(new byte[]{
                                (byte) idx,
                                (byte) delta
                        });
                    }
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
