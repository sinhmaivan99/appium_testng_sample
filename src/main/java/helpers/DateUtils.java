package helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe date/time utility methods.
 */
public final class DateUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy",
                    java.util.Locale.ENGLISH);

    private DateUtils() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns current date/time as a filesystem-safe string.
     */
    public static String getCurrentDate() {
        return LocalDateTime.now().toString()
                .replace(":", "_")
                .replace(" ", "_");
    }

    /**
     * Returns current date and time formatted as {@code dd/MM/yyyy HH:mm:ss}.
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns current date and time with all separators replaced by the given character.
     *
     * @param separatorChar the character to use as a separator
     */
    public static String getCurrentDateTimeCustom(String separatorChar) {
        return getCurrentDateTime()
                .replace("/", separatorChar)
                .replace(" ", separatorChar)
                .replace(":", separatorChar);
    }
}
