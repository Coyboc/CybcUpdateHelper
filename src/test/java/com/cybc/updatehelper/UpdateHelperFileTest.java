package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateFailedException;
import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateStepFailedException;
import com.cybc.updatehelper.impl.UpdatableTester;
import com.cybc.updatehelper.util.FileWriteHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class UpdateHelperFileTest {

    private File testFile;

    @Before
    public void createTestFile() {
        File testDir = new File("testFiles/");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        testFile = new File(testDir, "/test.csv");

        if (testFile.exists()) {
            testFile.delete();
        }

        FileWriteHelper.writeSingleLine("1", testFile);
    }

    @Test
    public void update() {
        UpdatableTester tester = new UpdatableTester(testFile);
        tester.onUpgrade();
    }

    @Test(expected = UpdateNullException.class)
    public void failureUpdateNullItem() {
        UpdatableTester tester = new UpdatableTester(testFile);
        tester.addUpdate(3, null);

        tester.onUpgrade();
    }

    @Test(expected = UpdateFailedException.class)
    public void failureUpdateLatestVersion() {
        UpdatableTester tester = new UpdatableTester(testFile);

        tester.setLatestUpdateVersion(1);
        tester.setTargetVersion(10);

        tester.onUpgrade();
    }

    @Test(expected = UpdateFailedException.class)
    public void failureUpdateWithClosedStorage() {
        final UpdatableTester tester = new UpdatableTester(testFile);
        tester.setTargetVersion(3);

        List<Update<File>> updates = new ArrayList<>();
        updates.add(new Update<File>() {
            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine("First", file);
            }

            @Override
            public int getUpdateVersion() {
                return 1;
            }

        });
        updates.add(new Update<File>() {
            @Override
            public int getUpdateVersion() {
                return 2;
            }

            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine("FAIL!", file);
                tester.setClosed(true);
            }

        });
        updates.add(new Update<File>() {
            @Override
            public int getUpdateVersion() {
                return 3;
            }

            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine("Third", file);
            }
        });
        tester.setUpdates(updates);
        tester.onUpgrade();
    }

    @Test(expected = UpdateStepFailedException.class)
    public void failureUpdateExceptionWhileUpdate() {
        final UpdatableTester tester = new UpdatableTester(testFile);
        tester.setTargetVersion(2);

        List<Update<File>> updates = new ArrayList<>();
        updates.add(new Update<File>() {
            @Override
            public int getUpdateVersion() {
                return 1;
            }

            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine("First", file);
            }
        });
        updates.add(new Update<File>() {
            @Override
            public int getUpdateVersion() {
                return 2;
            }

            @Override
            public void execute(File file) throws Exception {
                throw new RuntimeException("Test me!");
            }

        });
        tester.setUpdates(updates);
        tester.onUpgrade();
    }

    @Test
    public void startUpdaterWithSameStartAndTargetVersion() {
        final UpdatableTester tester = new UpdatableTester(testFile);
        tester.setTargetVersion(10);
        tester.setStartVersion(10);
        tester.onUpgrade();
    }

}
