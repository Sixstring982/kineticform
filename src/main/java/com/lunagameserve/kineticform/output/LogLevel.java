package com.lunagameserve.kineticform.output;

import java.io.PrintStream;

/**
 * Created by sixstring982 on 5/7/16.
 */
public enum LogLevel {
    INFO(0),
    DEBUG(1),
    WARNING(2),
    ERROR(3),
    DISABLED(4);

    private int value;

    LogLevel(int value) {
        this.value = value;
    }

    public void write(LogLevel current, String message, PrintStream target) {
        if (this.value >= current.value) {
            target.println(String.format(
                    "[%s]: %s",
                    toString().toUpperCase(),
                    message
            ));
        }
    }
}
