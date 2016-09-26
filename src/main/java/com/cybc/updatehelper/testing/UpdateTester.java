package com.cybc.updatehelper.testing;

import com.cybc.updatehelper.UpdateHelper;
import com.cybc.updatehelper.UpdateWorker;

/**
 * The implementation of for an advanced {@link UpdateTester}.
 *
 * @param <UpdateImpl>
 *         The implementing update test factory
 * @param <StorageToUpdate>
 *         the implementation for the storage
 */
public class UpdateTester<UpdateImpl extends UpdateTest<StorageToUpdate>, StorageToUpdate> extends UpdateHelper<UpdateImpl, StorageToUpdate> {

    public UpdateTester(UpdateWorker<UpdateImpl, StorageToUpdate> updatable) {
        super(updatable);
    }
}
