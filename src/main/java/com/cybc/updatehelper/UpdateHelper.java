package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateFailedException;
import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateStepFailedException;
import com.cybc.updatehelper.exceptions.UpdateValidationException;

import java.util.Collection;
import java.util.Iterator;

/**
 * Base class to simplify updates. It provides an implementation for iterating over the updates from the oldest version to the newest one. Also checks the update order for possible
 * multiple updates.
 *
 * @param <UpdateImpl>      the implementation of {@link Update}
 * @param <StorageToUpdate> the storage to update
 */
public class UpdateHelper<UpdateImpl extends Update<StorageToUpdate>, StorageToUpdate> {

    private final UpdateWorker<UpdateImpl, StorageToUpdate> updatable;

    /**
     * Creates a new {@link UpdateHelper} for the given {@link UpdateWorker}. The Helper will collect the needed information from the {@link UpdateWorker} for the updates.
     *
     * @param updatable the object (in most cases a database instance) to update.
     */
    public UpdateHelper(UpdateWorker<UpdateImpl, StorageToUpdate> updatable) {
        this.updatable = updatable;
    }

    /**
     * Iterates over given updates from {@link UpdateWorker#createUpdates()}.<p><b><code>newVersion</code> must be equals the latest update version, provided by {@link
     * UpdateWorker#getLatestUpdateVersion(StorageToUpdate)}</b></p> <p>Calls<br>{@link UpdateWorker#onPreUpdate(StorageToUpdate, Update)} before an {@link UpdateImpl} will be
     * executed <br>{@link UpdateWorker#onPostUpdate(StorageToUpdate, UpdateImpl)} when an {@link UpdateImpl} was finished successfully and <br>{@link
     * UpdateWorker#onUpgradingDone(StorageToUpdate)} when all {@link UpdateImpl}s were finished successfully.</p>
     *
     * @param storageToUpdate The storage to update.
     * @param oldVersion      The old storage version.
     * @param newVersion      The new storage version, must be equals the latest update version, provided by {@link UpdateWorker#getLatestUpdateVersion(StorageToUpdate)}
     *
     * @throws UpdateFailedException     when an update fails (Update item null or an Exception was thrown while updating)
     * @throws UpdateValidationException when the updates were provided in a wrong order
     * @throws UpdateNullException       When an update is null
     * @throws UpdateStepFailedException When a single update step failed.
     */
    public void onUpgrade(StorageToUpdate storageToUpdate, int oldVersion, int newVersion) throws UpdateFailedException, UpdateValidationException, UpdateNullException, UpdateStepFailedException {
        if (oldVersion == newVersion) {
            return; //nothing to do, db up to date
        }
        //TODO try to handle this by the framework
        final int latestUpdateVersion = updatable.getLatestUpdateVersion(storageToUpdate);
        if (latestUpdateVersion != newVersion) {
            throw new UpdateFailedException("Latest update version != new Storage Version! UpdatePool incompatible with newest Storage version! latestUpdateVersion[" + latestUpdateVersion + "] <= newVersion[" + newVersion + "]");
        }

        final Collection<UpdateImpl> updates = updatable.createUpdates();

        //check for valid updates
        final UpdateValidationResult updateValidationResult = validateUpdates(updates, latestUpdateVersion);
        updateValidationResult.throwIfCorrupted();

        int lastVersionUpdate = 0;
        for (UpdateImpl update : updates) {
            if (updatable.isStorageClosed(storageToUpdate)) {
                throw new UpdateFailedException("StorageConnection is closed! Does an update close the Storage? Last version update was: " + lastVersionUpdate);
            }

            final int targetVersion = update.getUpdateVersion();
            if (targetVersion > oldVersion) {
                try {

                    updatable.onPreUpdate(storageToUpdate, update);
                    update.execute(storageToUpdate);
                    updatable.onPostUpdate(storageToUpdate, update);

                    lastVersionUpdate = targetVersion;
                } catch (Exception e) {
                    throw new UpdateStepFailedException(update, e);
                }
            }
        }
        updatable.onUpgradingDone(storageToUpdate);
    }

    /**
     * Makes a check for correct ordered storage updates.
     *
     * @param <UpdateImpl>         The implementation of the update
     * @param updates              The updates for checking the correct order.
     * @param expectedFinalVersion the version the storage become have after applying every update
     *
     * @return {@link UpdateValidationResult} with Type {@link  UpdateValidationResult.Type#CORRECT} when the validation was successful, another {@link UpdateValidationResult.Type} otherwise.
     *
     * @throws UpdateNullException When an update is null
     */
    public static <UpdateImpl extends Update> UpdateValidationResult validateUpdates(Collection<UpdateImpl> updates, int expectedFinalVersion) throws UpdateNullException {
        if (updates == null) {
            throw new UpdateNullException("Collection of updates must not be null!");
        }
        if (updates.isEmpty()) {
            return UpdateValidationResult.forEmpty();
        }
        final Iterator<UpdateImpl> iterator = updates.iterator();
        UpdateImpl update = iterator.next();

        if (update == null) {
            throw new UpdateNullException("Update item is null! For first Update!");
        }

        int versionFrom = update.getUpdateVersion();
        int previousCheckVersion = update.getUpdateVersion();

        while (iterator.hasNext()) {
            update = iterator.next();

            if (update == null) {
                throw new UpdateNullException("Update item is null! Last version update was: " + previousCheckVersion);
            }
            final int versionTo = update.getUpdateVersion();
            if (versionFrom > versionTo) {
                return UpdateValidationResult.forWrong(versionFrom, versionTo);
            }
            if (versionFrom == versionTo) {
                return UpdateValidationResult.forEqualVersions(versionFrom);
            }
            //remember for next update version check
            previousCheckVersion = versionFrom = versionTo;
        }
        if (previousCheckVersion != expectedFinalVersion) {
            return UpdateValidationResult.forWrongFinalVersion(expectedFinalVersion, previousCheckVersion);
        }
        return UpdateValidationResult.forCorrect(expectedFinalVersion);
    }

    public static class UpdateValidationResult {

        public static UpdateValidationResult forWrong(int versionFrom, int toVersion) {
            UpdateValidationResult updateValidationResult = new UpdateValidationResult();
            updateValidationResult.wrongFromVersion = versionFrom;
            updateValidationResult.wrongToVersion = toVersion;
            updateValidationResult.type = Type.WRONG_ORDER;
            return updateValidationResult;
        }

        public static UpdateValidationResult forEqualVersions(int versionFrom) {
            UpdateValidationResult updateValidationResult = new UpdateValidationResult();
            updateValidationResult.wrongToVersion = updateValidationResult.wrongFromVersion = versionFrom;
            updateValidationResult.type = Type.EQUALS;
            return updateValidationResult;
        }

        public static UpdateValidationResult forCorrect(int expectedFinalVersion) {
            UpdateValidationResult updateValidationResult = new UpdateValidationResult();
            updateValidationResult.expectedFinalVersion = expectedFinalVersion;
            updateValidationResult.actualFinalVersion = expectedFinalVersion;
            updateValidationResult.type = Type.CORRECT;
            return updateValidationResult;
        }

        public static UpdateValidationResult forEmpty() {
            UpdateValidationResult updateValidationResult = new UpdateValidationResult();
            updateValidationResult.type = Type.EMPTY;
            return updateValidationResult;
        }

        public static UpdateValidationResult forWrongFinalVersion(int expectedVersion, int actualVersion) {
            UpdateValidationResult updateValidationResult = new UpdateValidationResult();
            updateValidationResult.expectedFinalVersion = expectedVersion;
            updateValidationResult.actualFinalVersion = actualVersion;
            updateValidationResult.type = Type.WRONG_FINAL;
            return updateValidationResult;
        }

        private enum Type {CORRECT, WRONG_ORDER, EMPTY, EQUALS, WRONG_FINAL}

        private int  wrongFromVersion;
        private int  wrongToVersion;
        private int  expectedFinalVersion;
        private int  actualFinalVersion;
        private Type type;

        private UpdateValidationResult() {
        }

        public boolean isCorrect() {
            return type == Type.CORRECT;
        }

        public boolean isWrongOrder() {
            return type == Type.WRONG_ORDER;
        }

        public boolean hasEqualVersions() {
            return type == Type.EQUALS;
        }

        public boolean isEmpty() {
            return type == Type.EMPTY;
        }

        public boolean isUnexpectedFinalVersion() {
            return type == Type.WRONG_FINAL;
        }

        /**
         * Use this method to interpret the {@link UpdateValidationResult} and throw an exception if the order is not correct. Incorrect orders could cause corrupted data inside the storage.
         *
         * @throws UpdateValidationException When this {@link UpdateValidationResult} is incorrect.
         */
        public void throwIfCorrupted() throws UpdateValidationException {
            if (isWrongOrder()) {
                throw UpdateValidationException.forWrongOrderedVersions(wrongFromVersion, wrongToVersion);
            }
            if (hasEqualVersions()) {
                throw UpdateValidationException.forEqualVersions(wrongFromVersion);
            }
            if (isEmpty()) {
                throw UpdateValidationException.forEmpty();
            }
            if (isUnexpectedFinalVersion()) {
                throw UpdateValidationException.forWrongFinalVersion(expectedFinalVersion, actualFinalVersion);
            }

        }
    }
}
