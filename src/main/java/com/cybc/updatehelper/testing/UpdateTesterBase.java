package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.UpdateHelper;
import com.cybc.updatehelper.UpdateWorker;
import com.cybc.updatehelper.testing.exceptions.TestUpdateExecutionFailedException;

import java.util.Collection;

/**
 * Base class to create own implementations of update testers
 *
 * @param <UpdateImpl>
 *         The implementing update test factory
 * @param <StorageToUpdate>
 *         the implementation for the storage
 */
public abstract class UpdateTesterBase<UpdateImpl extends UpdateTest<StorageToUpdate>, StorageToUpdate> implements UpdateWorker<UpdateImpl, StorageToUpdate> {

    private final UpdateWorker<UpdateImpl, StorageToUpdate> updatable;
    private final UpdateHelper<UpdateImpl, StorageToUpdate> updateHelper = new UpdateHelper<>(this);

    public UpdateTesterBase(UpdateWorker<UpdateImpl, StorageToUpdate> updatable) {
        this.updatable = updatable;
    }

    @Override
    public int getLatestUpdateVersion(StorageToUpdate storageToUpdate) {
        return updatable.getLatestUpdateVersion(storageToUpdate);
    }

    @Override
    public Collection<UpdateImpl> createUpdates() {
        return updatable.createUpdates();
    }

    @Override
    public void onPreUpdate(StorageToUpdate storageToUpdate, UpdateImpl update) {
        updatable.onPreUpdate(storageToUpdate, update);
    }

    public void onUpgrade(StorageToUpdate storageToUpdate, int oldVersion, int newVersion) {
        updateHelper.onUpgrade(storageToUpdate, oldVersion, newVersion);
    }

    @Override
    public void onUpgradingDone(StorageToUpdate storageToUpdate) {
        updatable.onUpgradingDone(storageToUpdate);
    }

    @Override
    public void onPostUpdate(StorageToUpdate storageToUpdate, UpdateImpl update) {
        UpdateTestExecutor<StorageToUpdate> test = update.createTestExecutor();

        if (test == null) {
            throw new TestUpdateExecutionFailedException("Test is null for: " + update);
        }

        executeTestUpdate(storageToUpdate, test);
    }

    /**
     * Is called when the update will be tested by the (@link UpdateTesterBase}.
     *
     * @param storageToUpdate
     *         The storage of the test
     * @param updateTest
     *         The test for the storage update
     */
    protected void executeTestUpdate(StorageToUpdate storageToUpdate, UpdateTestExecutor<StorageToUpdate> updateTest) {
        //inserting mock data
        updateTest.insertMockData(storageToUpdate);
        //make tests with inserted mock data
        updateTest.testConsistency(storageToUpdate);
    }
}
