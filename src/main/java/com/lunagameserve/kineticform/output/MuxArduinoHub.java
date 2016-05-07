package com.lunagameserve.kineticform.output;

import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sixstring982 on 5/5/16.
 */
public class MuxArduinoHub implements ArduinoHub {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;

    private Process process = null;
    private Thread processReader;
    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void connect() {
        try {
            process = Runtime.getRuntime().exec(new String[]{
                            "/home/sixstring982/Documents/Git/arduino-mux/bin/arduino-mux-0.0.1",
                            "/home/sixstring982/.kinetic-form/arduino-mux.conf"
                    });
            processReader = new Thread(new Runnable() {
                @Override
                public void run() {
                    final Process prc = process;
                    readProcessOutput(prc);
                }
            });
            running.set(true);

            processReader.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(TwitterCommandQueue queue) throws IOException {
        if (queue.isCommandAvailable()) {
            TwitterCommand command = queue.nextCommand();
            OutputStream out = new TeeOutputStream(
                    new FalseArduino(0),
                    process.getOutputStream()
            );
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
                List<Byte> bytes = arduinos.get(idx);
                Byte[] byteArray = new Byte[bytes.size()];
                bytes.toArray(byteArray);

                out.write(new byte[] {
                        idx.byteValue(),
                        (byte) bytes.size()
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
        if (running.get()) {
            running.set(false);
            System.out.println("Waiting for arduino-mux...");
            try {
                process.getOutputStream().close();
                process.waitFor();

                System.out.println("arduino-mux closed successfully. Joining read thread...");

                process = null;
                processReader.join();

                System.out.println("Read thread joined.");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readProcessOutput(Process process) {
        while (running.get()) {
            try {
                if (process.getInputStream().available() > 0) {
                    byte[] in = new byte[process.getInputStream().available()];
                    process.getInputStream().read(in);
                    System.out.write(in);
                    System.out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
