package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateOrderWrongException;

import java.util.Collection;

/**
 * Interface to create the needed updates and for getting information.
 *
 * @param <UpdateImpl>
 *         The implementation of {@link Update}
 * @param <StorageToUpdate>
 *         The storage you want to update (Like databases, Files or similar)
 */
public interface UpdateWorker<UpdateImpl extends Update<StorageToUpdate>, StorageToUpdate> {

    /**
     * Latest version of all updates. The latest update version must be equals newVersion in {@link UpdateHelper#onUpgrade(Object, int, int)}.
     *
     * @param storageToUpdate
     *         The storage to update.
     */
    int getLatestUpdateVersion(StorageToUpdate storageToUpdate);

    /**
     * Creates a collection of {@link UpdateImpl}. <p> <b>The implementation of this method should ensure that the updates are in the correct order or {@link
     * UpdateHelper#onUpgrade(StorageToUpdate, int, int)} will throw an {@link UpdateOrderWrongException}.</b> </p>
     *
     * @return Collection of {@link UpdateImpl}.
     *
     * @see UpdateHelper#onUpgrade(StorageToUpdate, int, int)
     * @see UpdateHelper#createOrderResultOf(Collection)
     */
    Collection<UpdateImpl> createUpdates();

    /**
     * Is called before an {@link UpdateImpl} is executed
     *
     * @param storageToUpdate
     *         The storage which got updated by {@link UpdateImpl}
     * @param update
     *         The {@link UpdateImpl}
     */
    void onPreUpdate(StorageToUpdate storageToUpdate, UpdateImpl update);

    /**
     * Is called after every {@link UpdateImpl}
     *
     * @param storageToUpdate
     *         The storage which got updated by {@link UpdateImpl}
     * @param update
     *         The executed {@link UpdateImpl}
     */
    void onPostUpdate(StorageToUpdate storageToUpdate, UpdateImpl update);

    /**
     * Is called when all {@link UpdateImpl}s were successfully executed.
     *
     * @param storageToUpdate
     *         The storage which was updated.
     */
    void onUpgradingDone(StorageToUpdate storageToUpdate);

    /**
     * Checks for a closed Storage
     *
     * @param storageToUpdate
     *         The storage for the check
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isClosed(StorageToUpdate storageToUpdate);
}
