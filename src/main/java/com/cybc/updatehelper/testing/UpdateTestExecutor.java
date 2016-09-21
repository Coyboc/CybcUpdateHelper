package com.cybc.updatehelper.testing;

public interface UpdateTestExecutor<StorageToUpdate> {

    /**
     * Test here the result of the update
     *
     * @param storageToUpdate
     *         the storage to update
     */
    void testConsistency(StorageToUpdate storageToUpdate);

    /**
     * Use this method to insert mock data into the storageToUpdate.
     *
     * @param storageToUpdate
     *         the storage to update
     */
    void insertMockData(StorageToUpdate storageToUpdate);

}
