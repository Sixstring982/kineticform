package com.lunagameserve.kineticform.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sixstring982 on 5/5/16.
 */
public class TeeOutputStream extends OutputStream {
    private OutputStream a;
    private OutputStream b;

    public TeeOutputStream(OutputStream a, OutputStream b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void write(int b) throws IOException {
        this.a.write(b);
        this.b.write(b);
    }
}
