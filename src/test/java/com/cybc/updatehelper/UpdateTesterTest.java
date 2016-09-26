package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateFailedException;
import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateStepFailedException;
import com.cybc.updatehelper.impl.UpdatableTester;
import com.cybc.updatehelper.impl.UpdateTesterImpl;
import com.cybc.updatehelper.util.FileWriteHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateTesterTest {

    private File testFile;

    @Before
    public void createTestFile() {
        File testDir = new File("testFiles/");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        testFile = new File(testDir, "/testUpdates.csv");

        if (testFile.exists()) {
            testFile.delete();
        }

        FileWriteHelper.writeSingleLine("1", testFile);
    }

    @Test
    public void updateTests(){
        UpdateTesterImpl tester = new UpdateTesterImpl(testFile);
        tester.onUpgrade();
    }

    @Test
    public void writeEmptyLines(){
        FileWriteHelper.addEmptyLines(100, testFile);
    }
}
