package com.cybc.updatehelper.testing;

/**
 * An executor for update tests
 *
 * @param <StorageToUpdate>
 *         The Storage to update
 */
public interface UpdateTestExecutor<StorageToUpdate> {

    /**
     * Use this method to insert mock data into the storageToUpdate.
     *
     * @param storageToUpdate
     *         the storage to update
     */
    void insertMockData(StorageToUpdate storageToUpdate);

    /**
     * Test here the result of the update
     *
     * @param storageToUpdate
     *         the storage to update
     */
    void testConsistency(StorageToUpdate storageToUpdate);

}
