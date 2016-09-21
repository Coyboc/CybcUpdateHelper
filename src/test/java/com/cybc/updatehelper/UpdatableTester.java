package com.cybc.updatehelper;

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

public class UpdatableTester implements UpdateWorker<Update<File>, File> {

    private static final int VERSION_LINE   = 0;
    public static final  int TARGET_VERSION = 10;
    private final File file;
    private final UpdateHelper<Update<File>, File> fileUpdateHelper = new UpdateHelper<>(this);
    private final LinkedList<Update<File>> updateFactories;

    private int targetVersion       = TARGET_VERSION;
    private int latestUpdateVersion = -1;
    private boolean isClosed;

    public UpdatableTester(File file) {
        this.file = file;

        updateFactories = new LinkedList<>();
        for (int i = 1; i < TARGET_VERSION + 1; i++) {
            updateFactories.add(createUpdate(i));
        }
    }

    public void onUpgrade() {
        fileUpdateHelper.onUpgrade(file, getStartVersion(), targetVersion);
    }

    private int getStartVersion() {
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
    public Collection<Update<File>> createUpdates() {
        return updateFactories;
    }

    @Override
    public void onPreUpdate(File file, Update<File> update) {
    }

    @Override
    public void onPostUpdate(File file, Update<File> update) {
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
    public boolean isClosed(File file) {
        return isClosed;
    }

    private Update<File> createUpdate(final int version) {
        return new Update<File>() {
            @Override
            public int getUpdateVersion() {
                return version;
            }

            @Override
            public void execute(File file) throws Exception {
                FileWriteHelper.writeSingleLine(version + " - HELLO", file);
                FileWriteHelper.changeSingleLine(0, String.valueOf(version), file);
            }
        };
    }

    public void addUpdate(int position, Update<File> update) {
        updateFactories.add(position, update);
    }

    public void setUpdates(Collection<Update<File>> updateFactories) {
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
}
