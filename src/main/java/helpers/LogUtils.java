package helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Centralized logging utility backed by Log4j2.
 * <p>
 * Uses {@link StackWalker} to automatically resolve the calling class name so
 * log output shows the actual caller instead of "LogUtils".
 * </p>
 */
public final class LogUtils {

    private LogUtils() {
        // Utility class — prevent instantiation
    }

    private static Logger getLogger() {
        return LogManager.getLogger(
                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass());
    }

    public static void info(String message) {
        getLogger().info(message);
    }

    public static void info(Object object) {
        getLogger().info(object);
    }

    public static void warn(String message) {
        getLogger().warn(message);
    }

    public static void warn(Object object) {
        getLogger().warn(object);
    }

    public static void error(String message) {
        getLogger().error(message);
    }

    public static void error(String message, Throwable t) {
        getLogger().error(message, t);
    }

    public static void error(Object object) {
        getLogger().error(object);
    }

    public static void fatal(String message) {
        getLogger().fatal(message);
    }

    public static void debug(String message) {
        getLogger().debug(message);
    }

    public static void debug(Object object) {
        getLogger().debug(object);
    }
}
