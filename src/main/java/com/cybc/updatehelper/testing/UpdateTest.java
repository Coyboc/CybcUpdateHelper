package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.Update;

/**
 * Interface to test a special update.
 */
public interface UpdateTest<StorageToUpdate> extends Update<StorageToUpdate> {

    /**
     * Creates a test for this update.
     *
     * @return An {@link UpdateTest} for this update
     */
    UpdateTestExecutor<StorageToUpdate> createTestExecutor();
}
