package com.lunagameserve.kineticform.output;

import java.io.PrintStream;

/**
 * Created by sixstring982 on 5/7/16.
 */
public class Log {
    private static LogLevel level;

    private static PrintStream target;

    public static void setLevel(LogLevel level) {
        Log.level = level;
    }

    public static void setTarget(PrintStream target) {
        Log.target = target;
    }

    public static void i(String message) {
        LogLevel.INFO.write(level, message, target);
    }

    public static void d(String message) {
        LogLevel.DEBUG.write(level, message, target);
    }

    public static void w(String message) {
        LogLevel.WARNING.write(level, message, target);
    }

    public static void e(String message) {
        LogLevel.ERROR.write(level, message, target);
    }
}
