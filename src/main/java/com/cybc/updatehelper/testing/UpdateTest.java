package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.Update;

/**
 * Interface to test a special update.
 */
public interface UpdateTest<StorageToUpdate> {

    /**
     * Use this method to insert mock data into the storageToUpdate.
     *
     * @param storageToUpdate the storage to update
     */
    void insertMockData(StorageToUpdate storageToUpdate);

    /**
     * Test here the result of the update
     *
     * @param storageToUpdate the storage to update
     */
    void testConsistency(StorageToUpdate storageToUpdate);

    /**
     * @return the applicable update, that this test should validate
     */
    Update<StorageToUpdate> getUpdateToTest();

}
