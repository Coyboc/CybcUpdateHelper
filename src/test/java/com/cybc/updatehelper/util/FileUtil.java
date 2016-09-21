package com.cybc.updatehelper.util;

import java.io.File;
import java.io.IOException;

/**
 * Created by Steve Burkert on 8/14/16
 */
public class FileUtil {

    /**
     * Will either load or create the directory.
     *
     * @return the directory applicable to the path, assuming that parent dirs exists.
     */
    public static File loadOrCreateDir(final String dirPath) {
        return loadOrCreateDirs(dirPath, false);

    }

    /**
     * Will either load or create the directory and create parent directories if needed.
     *
     * @return the directory applicable to the path
     */
    public static File loadOrCreateDirs(final String dirPath) {
        return loadOrCreateDirs(dirPath, true);
    }

    private static File loadOrCreateDirs(String dirPath, final boolean createParents) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            return dir;
        } else {
            if (createParents) {
                dir.mkdirs();
            } else {
                dir.mkdir();
            }
            return dir;
        }
    }

    public static File loadOrCreateFile(final String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                return file;
            } else {
                return null;
            }
        } else {
            final String parent = file.getParent();
            if (parent != null) {
                loadOrCreateDirs(parent);
            }
            file.createNewFile();
            return file;
        }
    }

    public static File createNewFileHard(final String filePath) {
        File newFile = new File(filePath);
        try {
            if (!newFile.createNewFile()) {
                if (!newFile.delete() || !newFile.createNewFile()) {
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return newFile;
    }

    /**
     * @return true if the file or the directory and any file in the directory was deleted
     */
    public static boolean deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            return true;
        }
        if (fileOrDirectory.isDirectory()) {
            boolean dirDeleted = true;
            for (File file : fileOrDirectory.listFiles()) {
                dirDeleted = dirDeleted && deleteRecursive(file);
            }
            return dirDeleted && fileOrDirectory.delete();
        } else {
            return fileOrDirectory.delete();
        }
    }

}
