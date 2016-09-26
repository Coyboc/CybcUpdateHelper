package com.cybc.updatehelper;

/**
 * The interface fo an implementation of an update for a storage
 *
 * @param <StorageToUpdate>
 *         The implementation of the storage which gets this update.
 */
public interface Update<StorageToUpdate> {

    /**
     * The execution of the Update. Implement here your storage update logic.
     *
     * @param storageToUpdate
     *         The storage which should get updated
     */
    void execute(StorageToUpdate storageToUpdate) throws Exception;

    /**
     * The target version of this Update.
     */
    int getUpdateVersion();

}
