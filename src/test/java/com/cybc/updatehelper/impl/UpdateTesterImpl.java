package com.cybc.updatehelper.impl;

import static org.junit.Assert.assertEquals;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateHelper;
import com.cybc.updatehelper.UpdateWorker;
import com.cybc.updatehelper.testing.UpdateTest;
import com.cybc.updatehelper.testing.UpdateTestExecutor;
import com.cybc.updatehelper.testing.UpdateTester;
import com.cybc.updatehelper.util.FileReadHelper;
import com.cybc.updatehelper.util.FileWriteHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UpdateTesterImpl implements UpdateWorker<UpdateTest<File>, File> {

    private static final int VERSION_LINE   = 1;
    public static final  int TARGET_VERSION = 10;
    private final File file;
    private final UpdateTester<UpdateTest<File>, File> fileUpdateHelper = new UpdateTester<>(this);
    private final LinkedList<UpdateTest<File>> updateFactories;

    private int targetVersion       = TARGET_VERSION;
    private int startVersion        = -1;
    private int latestUpdateVersion = -1;
    private boolean isClosed;

    public UpdateTesterImpl(File file) {
        this.file = file;

        updateFactories = new LinkedList<>();
        for (int i = 1; i < TARGET_VERSION + 1; i++) {
            updateFactories.add(createUpdate(i));
        }
        FileWriteHelper.addEmptyLines(TARGET_VERSION + 1, file);
    }

    public void onUpgrade() {
        fileUpdateHelper.onUpgrade(file, getStartVersion(), targetVersion);
    }

    private int getStartVersion() {
        if (startVersion >= 0) {
            return startVersion;
        }
        return Integer.parseInt(FileReadHelper.readLine(VERSION_LINE, file));
    }

    @Override
    public int getLatestUpdateVersion(File file) {
        if (latestUpdateVersion > 0) {
            return latestUpdateVersion;
        }
        int latestVersion = 0;
        for (Update update : updateFactories) {
            if (update == null) {
                continue;
            }
            final int updateVersion = update.getUpdateVersion();
            if (updateVersion > latestVersion) {
                latestVersion = updateVersion;
            }
        }
        return latestVersion;
    }

    @Override
    public Collection<UpdateTest<File>> createUpdates() {
        return updateFactories;
    }

    @Override
    public void onPreUpdate(File file, UpdateTest<File> update) {
    }

    @Override
    public void onPostUpdate(File file, UpdateTest<File> update) {
    }

    @Override
    public void onUpgradingDone(File file) {
        System.out.println("Done. - " + file.getAbsolutePath());
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStorageClosed(File file) {
        return isClosed;
    }

    private UpdateTest<File> createUpdate(final int version) {
        return new UpdateTest<File>() {

            int line = version - 1;

            @Override
            public UpdateTestExecutor<File> createTestExecutor() {
                final String readLine = FileReadHelper.readLine(line, file);
                final String mockData = readLine + " MOCKED!";
                return new UpdateTestExecutor<File>() {
                    @Override
                    public void testConsistency(File file) {
                        final String readLine = FileReadHelper.readLine(line, file);
                        assertEquals(readLine, mockData);
                    }

                    @Override
                    public void insertMockData(File file) {
                        FileWriteHelper.changeSingleLine(line, mockData, file);
                    }
                };
            }

            @Override
            public int getUpdateVersion() {
                return version;
            }

            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine(line, version + " - HELLO", file);
                FileWriteHelper.changeSingleLine(VERSION_LINE, String.valueOf(version), file);
            }
        };
    }

    public void addUpdate(int position, UpdateTest<File> update) {
        updateFactories.add(position, update);
    }

    public void setUpdates(Collection<UpdateTest<File>> updateFactories) {
        this.updateFactories.clear();
        this.updateFactories.addAll(updateFactories);
    }

    public void setLatestUpdateVersion(int latestUpdateVersion) {
        this.latestUpdateVersion = latestUpdateVersion;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public void setTargetVersion(int targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void setStartVersion(int startVersion) {
        this.startVersion = startVersion;
    }
}
