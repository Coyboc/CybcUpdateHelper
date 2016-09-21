package com.cybc.updatehelper;

/**
 * Abstract implementation for an {@link UpdateHelper}.
 *
 * @param <UpdateImpl>
 *         the implementation of {@link Update}
 * @param <StorageToUpdate>
 *         the storage to update
 */
public class UpdateHelper<UpdateImpl extends Update<StorageToUpdate>, StorageToUpdate> extends UpdateHelperBase<UpdateImpl, StorageToUpdate> {

    /**
     * Creates a new {@link UpdateHelper} for the given {@link UpdateWorker}. The Helper will collect the needed information from the {@link UpdateWorker} for the updates.
     *
     * @param updatable
     *         the object (in most cases a database instance) to update.
     */
    public UpdateHelper(UpdateWorker<UpdateImpl, StorageToUpdate> updatable) {
        super(updatable);
    }
}
