package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateValidationException;

import java.util.Collection;

/**
 * Entry point of the update helper. An implementation of UpdateWorker will be the central
 * instance that, creates a collection of updates, identifies the latest version and will get informed
 * about the update states.
 *
 * @param <UpdateImpl>      The implementation of {@link Update}
 * @param <StorageToUpdate> The storage you want to update (Like databases, Files or similar)
 */
public interface UpdateWorker<UpdateImpl extends Update<StorageToUpdate>, StorageToUpdate> {

    /**
     * Latest version of all updates. The latest update version must be equals newVersion in {@link UpdateHelper#onUpgrade(Object, int, int)}.
     *
     * @param storageToUpdate The storage to update.
     */
    int getLatestUpdateVersion(StorageToUpdate storageToUpdate);

    /**
     * Creates a collection of {@link UpdateImpl}. <p> <b>The implementation of this method should ensure that the updates are in the correct order or {@link
     * UpdateHelper#onUpgrade(StorageToUpdate, int, int)} will throw an {@link UpdateValidationException}.</b> </p>
     *
     * @return Collection of {@link UpdateImpl}.
     *
     * @see UpdateHelper#onUpgrade(StorageToUpdate, int, int)
     * @see UpdateHelper#validateUpdates(Collection, int)
     */
    Collection<UpdateImpl> createUpdates();

    /**
     * Is called before an {@link UpdateImpl} is executed
     *
     * @param storageToUpdate The storage which got updated by {@link UpdateImpl}
     * @param update          The {@link UpdateImpl}
     */
    void onPreUpdate(StorageToUpdate storageToUpdate, UpdateImpl update);

    /**
     * Is called after every {@link UpdateImpl}
     *
     * @param storageToUpdate The storage which got updated by {@link UpdateImpl}
     * @param update          The executed {@link UpdateImpl}
     */
    void onPostUpdate(StorageToUpdate storageToUpdate, UpdateImpl update);

    /**
     * Is called when all {@link UpdateImpl}s were successfully executed.
     *
     * @param storageToUpdate The storage which was updated.
     */
    void onUpgradingDone(StorageToUpdate storageToUpdate);

    /**
     * Checks for a closed Storage
     *
     * @param storageToUpdate The storage for the check
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isStorageClosed(StorageToUpdate storageToUpdate); //TODO: do we really need this? Won't it crash anyway?
}
