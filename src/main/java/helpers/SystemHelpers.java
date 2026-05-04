package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Cross-platform system / filesystem utilities.
 */
public final class SystemHelpers {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    private SystemHelpers() {
        // Utility class — prevent instantiation
    }

    /**
     * Converts an input string to a filesystem-safe slug.
     *
     * @param input source string (must not be {@code null})
     * @return lowercase slug with whitespace replaced by underscores
     */
    public static String makeSlug(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("_");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        return NON_LATIN.matcher(normalized).replaceAll("").toLowerCase(Locale.ENGLISH);
    }

    /** Returns the current working directory with a trailing separator. */
    public static String getCurrentDir() {
        return System.getProperty("user.dir") + File.separator;
    }

    /**
     * Creates a folder (and any required parents) if it does not exist.
     *
     * @return {@code true} if a new folder was created, {@code false} if it already existed
     */
    public static boolean createFolder(String path) {
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            return false;
        }
        boolean created = folder.mkdirs();
        if (created) {
            LogUtils.debug("Created folder: " + path);
        } else {
            LogUtils.warn("Failed to create folder: " + path);
        }
        return created;
    }

    /**
     * Kills any process listening on the given TCP port.
     */
    public static void killProcessOnPort(String port) {
        try {
            String[] command = IS_WINDOWS
                    ? new String[]{"cmd", "/c", "netstat -ano | findstr :" + port}
                    : new String[]{"sh", "-c", "lsof -ti :" + port};
            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String pid = extractPid(line);
                    if (pid == null || pid.isBlank()) continue;
                    Runtime.getRuntime().exec(IS_WINDOWS
                            ? new String[]{"taskkill", "/F", "/PID", pid}
                            : new String[]{"kill", "-9", pid});
                    LogUtils.info("Killed process " + pid + " on port " + port);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LogUtils.error("Failed to kill process on port " + port, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Starts an Appium server with a curated plugin set. Output is streamed to the logger.
     */
    public static void startAppiumWithPlugins(String server, String port) {
        ProcessBuilder builder = new ProcessBuilder(
                "appium",
                "-a", server,
                "-p", port,
                "-ka", "800",
                "--use-plugins", "appium-reporter-plugin,element-wait,gestures,device-farm,appium-dashboard",
                "-pa", "/",
                "--plugin-device-farm-platform", "android"
        ).redirectErrorStream(true);

        try {
            Process process = builder.start();
            LogUtils.info("Appium server started with plugins");

            Thread streamReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LogUtils.debug(line);
                    }
                } catch (IOException e) {
                    LogUtils.error("Error reading Appium output", e);
                }
            }, "appium-output-reader");
            streamReader.setDaemon(true);
            streamReader.start();
        } catch (IOException e) {
            LogUtils.error("Failed to start Appium with plugins", e);
        }
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    /**
     * Extracts the PID from an OS-specific listing line.
     * On Windows the netstat line is: {@code   TCP    0.0.0.0:4723   ...   LISTENING       12345}
     * (PID is the last token). On Linux/Mac with {@code lsof -ti}, the whole line is the PID.
     */
    private static String extractPid(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return null;
        if (!IS_WINDOWS) {
            return trimmed.split("\\s+")[0];
        }
        String[] tokens = trimmed.split("\\s+");
        return tokens.length == 0 ? null : tokens[tokens.length - 1];
    }
}
