package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateHelper;
import com.cybc.updatehelper.UpdateWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateTestRunner<Storage> implements UpdateWorker<Update<Storage>, Storage> {

    private final UpdateHelper<Update<Storage>, Storage> helper;
    private final StorageProvider<Storage>               storageProvider;
    private final Map<Integer, UpdateTest<Storage>>      updateMap;
    private final Collection<UpdateTest<Storage>>        testUpdatesSorted;
    private       int                                    newVersion;

    public interface StorageProvider<Storage> {

        void setVersionBy(Update<Storage> lastUpdate, Storage storage);

        boolean isStorageClosed(Storage storage);

        void closeStorage(Storage storage);

    }

    public UpdateTestRunner(StorageProvider<Storage> storageProvider, Collection<UpdateTest<Storage>> testUpdates) {
        this.storageProvider = storageProvider;
        this.updateMap = createUpdateMap(testUpdates);
        this.testUpdatesSorted = testUpdates;
        this.helper = new UpdateHelper<>(this);
    }

    public void runTestUpdates(Storage storage, int oldVersion, int newVersion) {
        this.newVersion = newVersion;
        helper.onUpgrade(storage, oldVersion, newVersion);
    }

    private Map<Integer, UpdateTest<Storage>> createUpdateMap(Collection<UpdateTest<Storage>> testUpdates) {
        Map<Integer, UpdateTest<Storage>> updateMap = new HashMap<>();

        for (UpdateTest<Storage> test : testUpdates) {
            updateMap.put(test.getUpdateToTest().getUpdateVersion(), test);
        }
        return updateMap;
    }

    @Override
    public int getLatestUpdateVersion(Storage storage) {
        return newVersion;
    }

    @Override
    public Collection<Update<Storage>> createUpdates() {
        List<Update<Storage>> updates = new ArrayList<>();
        for (UpdateTest<Storage> item : testUpdatesSorted) {
            updates.add(item.getUpdateToTest());
        }
        return updates;
    }

    @Override
    public void onPreUpdate(Storage storage, Update<Storage> update) {
        //nothing to do
    }

    // basically the interesting stuff happens here:

    @Override
    public void onPostUpdate(Storage storage, Update<Storage> update) {
        storageProvider.setVersionBy(update, storage);

        UpdateTest<Storage> testUpdate = updateMap.get(update.getUpdateVersion());
        //inserting mock data
        testUpdate.insertMockData(storage);
        //make tests with inserted mock data
        testUpdate.testConsistency(storage);
    }

    @Override
    public void onUpgradingDone(Storage storage) {
        this.storageProvider.closeStorage(storage);
    }

    @Override
    public boolean isStorageClosed(Storage storage) {
        return storageProvider.isStorageClosed(storage);
    }
}
