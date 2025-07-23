package helpers;

import java.io.File;

public class DeleteFilesInMultipleFolders {
    public static void deleteFilesInFolder() {
        String[] folders = { "exports/screenshots", "exports/videos" };

        for (String folderPath : folders) {
            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile()) {
                            if (file.delete()) {
                                System.out.println("Đã xóa: " + file.getAbsolutePath());
                            } else {
                                System.out.println("Không thể xóa: " + file.getAbsolutePath());
                            }
                        }
                    }
                } else {
                    System.out.println("Thư mục '" + folderPath + "' trống hoặc không đọc được.");
                }
            } else {
                System.out.println("Thư mục '" + folderPath + "' không tồn tại hoặc không phải là thư mục.");
            }
        }
    }
}
