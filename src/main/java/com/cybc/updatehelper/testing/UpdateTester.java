package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateHelper;
import com.cybc.updatehelper.UpdateWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateTester<Storage> implements UpdateWorker<Update<Storage>, Storage> {

    private final UpdateHelper<Update<Storage>, Storage>    helper;
    private final StorageProvider<Storage>                  storageProvider;
    private final Map<Update<Storage>, UpdateTest<Storage>> updateMap;
    private final Collection<UpdateTest<Storage>>           testUpdatesSorted;
    private       int                                       newVersion;

    public interface StorageProvider<Storage> {
        Storage createTemporaryStorage();

        boolean isStorageClosed(Storage storage);

        void closeStorage(Storage storage);
    }

    public UpdateTester(StorageProvider<Storage> storageProvider, Collection<UpdateTest<Storage>> testUpdates) {
        this.storageProvider = storageProvider;
        this.updateMap = createUpdateMap(testUpdates);
        this.testUpdatesSorted = testUpdates;
        this.helper = new UpdateHelper<>(this);
    }

    public void runTestUpdates(final int oldVersion, final int newVersion) {
        this.newVersion = newVersion;
        helper.onUpgrade(storageProvider.createTemporaryStorage(), oldVersion, newVersion);
    }

    private Map<Update<Storage>, UpdateTest<Storage>> createUpdateMap(Collection<UpdateTest<Storage>> testUpdates) {
        Map<Update<Storage>, UpdateTest<Storage>> updateMap = new HashMap<>();
        for (UpdateTest<Storage> test : testUpdates) {
            updateMap.put(test.getUpdateToTest(), test);
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
        UpdateTest<Storage> testUpdate = updateMap.get(update);
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
