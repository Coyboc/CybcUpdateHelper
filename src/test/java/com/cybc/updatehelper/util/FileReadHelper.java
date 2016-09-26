package com.cybc.updatehelper.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileReadHelper {

    public static String readLine(int lineNumber, File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File not exists.");
        }
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("Lines starting at 1!");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            return lines.get(lineNumber - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
