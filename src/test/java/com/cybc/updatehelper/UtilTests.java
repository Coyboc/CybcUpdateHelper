package com.cybc.updatehelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cybc.updatehelper.util.FileReadHelper;
import com.cybc.updatehelper.util.FileWriteHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(JUnit4.class)
public class UtilTests {

    private File testDir;

    @Before
    public void prepare() {
        testDir = new File("testFiles/");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
    }

    @Test
    public void addEmptyLines() throws Exception {
        File f = forceCreateFile(testDir, "EmptyLines.txt");

        try {
            final int linesAmount = 10;
            FileWriteHelper.addEmptyLines(linesAmount, f);

            final List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), StandardCharsets.UTF_8);
            assertEquals(linesAmount, lines.size() + 1);
        } finally {
            assertTrue(f.delete());
        }
    }

    @Test
    public void writeALine() throws Exception {
        File f = forceCreateFile(testDir, "WriteLines.txt");
        try {
            final int lineNumber = 3;
            final String aString = "TEST";
            FileWriteHelper.writeSingleLine(lineNumber, aString, f);
            assertEquals(aString, FileReadHelper.readLine(lineNumber, f));
        } finally {
            assertTrue(f.delete());
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void failureWriteLineNumberStart() throws Exception {
        FileWriteHelper.writeSingleLine(0, "--", new File(testDir, "Failure."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failureReadLineFileExists() throws Exception {
        FileReadHelper.readLine(1, new File(testDir, "Failure."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failureReadLineStartLineNumber() throws Exception {
        File f = forceCreateFile(testDir, "WriteLines.txt");
        try {
            final String aString = "TEST";
            FileWriteHelper.writeSingleLine(1, aString, f);
            assertEquals(aString, FileReadHelper.readLine(1, f));

            //crash:
            FileReadHelper.readLine(0, f);

        } finally {
            assertTrue(f.delete());
        }
    }

    private File forceCreateFile(File dir, String name) throws IOException {
        File f = new File(dir, name);
        if (f.exists()) {
            assertTrue(f.delete());
        }
        assertTrue(f.createNewFile());
        return f;
    }

}
