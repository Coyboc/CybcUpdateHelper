package com.cybc.updatehelper.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileWriteHelper {

    public static void writeSingleLine(String toWriteLine, File file) {
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(toWriteLine);
            fileWriter.write('\n');
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeSingleLine(int lineNumber, String toWriteLine, File file) {
        try {
            if (!file.exists()) {
                throw new IllegalArgumentException("File not exists!");
            }
            final Path path = Paths.get(file.getPath());
            List<String> fileLines = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
            final int fileLineNumbers = fileLines.size();
            if (lineNumber > fileLineNumbers - 1) {
                throw new IllegalArgumentException("lineNumber can't be bigger then file line numbers! lineNumber[" + lineNumber + "] > fileLineNumber[" + fileLineNumbers + "]");
            }
            fileLines.set(lineNumber, toWriteLine);
            Files.write(path, fileLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
