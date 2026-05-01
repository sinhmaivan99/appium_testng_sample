package helpers;

import constants.ConfigData;

import java.io.File;

/**
 * Utility for cleaning up output folders (screenshots, videos) before a test run.
 */
public final class FileCleanupHelper {

    private FileCleanupHelper() {
        // Utility class — prevent instantiation
    }

    /**
     * Deletes all files inside the screenshot and video output folders.
     */
    public static void deleteOutputFiles() {
        String[] folders = {ConfigData.SCREENSHOT_PATH, ConfigData.RECORD_VIDEO_PATH};

        for (String folderPath : folders) {
            File folder = new File(folderPath);

            if (!folder.exists() || !folder.isDirectory()) {
                LogUtils.warn("Output folder does not exist or is not a directory: " + folderPath);
                continue;
            }

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                LogUtils.info("Output folder is empty: " + folderPath);
                continue;
            }

            for (File file : files) {
                if (file.isFile()) {
                    if (file.delete()) {
                        LogUtils.debug("Deleted: " + file.getAbsolutePath());
                    } else {
                        LogUtils.warn("Could not delete: " + file.getAbsolutePath());
                    }
                }
            }
            LogUtils.info("Cleaned output folder: " + folderPath);
        }
    }
}
