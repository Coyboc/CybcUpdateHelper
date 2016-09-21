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
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            return lines.get(lineNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
