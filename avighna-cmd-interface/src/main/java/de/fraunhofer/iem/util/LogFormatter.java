package de.fraunhofer.iem.util;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Motivated from here https://stackoverflow.com/questions/53211694/change-color-and-format-of-java-util-logging-logger-output-in-eclipse
 */
public class LogFormatter extends Formatter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        builder.append(ANSI_WHITE);
        builder.append(generateDataAndTime(record.getMillis()));
        builder.append(ANSI_RESET);

        Level level = record.getLevel();

        if (level == Level.SEVERE) {
            builder.append(ANSI_RED);
        } else if (level == Level.WARNING) {
            builder.append(ANSI_YELLOW);
        } else {
            builder.append(ANSI_GREEN);
        }

        builder.append(" ");
        builder.append(record.getLevel().getName());
        builder.append(
                Collections
                        .nCopies(8 - level.toString().length(), "")
                        .toString()
                        .replaceAll("\\[", "")
                        .replaceAll("]", "")
                        .replaceAll(",", ""));
        builder.append(ANSI_RESET);

        builder.append(" [");

        builder.append(ANSI_BLUE);
        builder.append(record.getSourceMethodName());
        builder.append(ANSI_RESET);

        builder.append("]");

        builder.append(ANSI_PURPLE);
        builder.append("(").append(record.getSourceClassName()).append(")");
        builder.append(ANSI_RESET);

        builder.append(": ");
        builder.append(record.getMessage());

        Object[] params = record.getParameters();

        if (params != null) {
            builder.append("\t");
            for (int i = 0; i < params.length; i++) {
                builder.append(params[i]);
                if (i < params.length - 1) builder.append(", ");
            }
        }

        builder.append(ANSI_RESET);
        builder.append("\n");

        return builder.toString();
    }

    private String generateDataAndTime(long millisecs) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(millisecs));
    }
}
