package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

/**
 * Utility class for logging
 *
 * @author Ranjith Krishnamurthy
 */
public class LoggerUtil {
    private static Logger LOGGER = null;

    public static Logger getLOGGER() {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(MainInterface.class.getName());

            LOGGER.setUseParentHandlers(false);

            if (LOGGER.getHandlers().length == 0) {
                ConsoleHandler handler = new ConsoleHandler();

                Formatter formatter = new LogFormatter();
                handler.setFormatter(formatter);

                LoggerUtil.LOGGER.addHandler(handler);
            }
        }

        return LOGGER;
    }
}
