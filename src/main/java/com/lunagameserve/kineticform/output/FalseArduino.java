package com.lunagameserve.kineticform.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sixstring982 on 4/23/16.
 */
public class FalseArduino extends OutputStream {
    private final int id;

    public FalseArduino(int id) {
        this.id = id;
    }

    @Override
    public void write(int b) throws IOException {
        System.out.println(String.format("[Arduino %2d]: %x", id, b));
    }
}
