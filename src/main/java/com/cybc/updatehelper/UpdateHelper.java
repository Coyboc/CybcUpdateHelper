package com.cybc.updatehelper;

import com.cybc.updatehelper.exceptions.UpdateFailedException;
import com.cybc.updatehelper.exceptions.UpdateNullException;
import com.cybc.updatehelper.exceptions.UpdateOrderWrongException;
import com.cybc.updatehelper.exceptions.UpdateStepFailedException;

import java.util.Collection;
import java.util.Iterator;

/**
 * Base class to simplify updates. It provides an implementation for iterating over the updates from the oldest version to the newest one. Also checks the update order for possible
 * multiple updates.
 *
 * @param <UpdateImpl>
 *         the implementation of {@link Update}
 * @param <StorageToUpdate>
 *         the storage to update
 */
public class UpdateHelper<UpdateImpl extends Update<StorageToUpdate>, StorageToUpdate> {

    private final UpdateWorker<UpdateImpl, StorageToUpdate> updatable;

    /**
     * Creates a new {@link UpdateHelper} for the given {@link UpdateWorker}. The Helper will collect the needed information from the {@link UpdateWorker} for the updates.
     *
     * @param updatable
     *         the object (in most cases a database instance) to update.
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
     * @param storageToUpdate
     *         The storage to update.
     * @param oldVersion
     *         The old storage version.
     * @param newVersion
     *         The new storage version, must be equals the latest update version, provided by {@link UpdateWorker#getLatestUpdateVersion(StorageToUpdate)}
     *
     * @throws UpdateFailedException
     *         when an update fails (Update item null or an Exception was thrown while updating)
     * @throws UpdateOrderWrongException
     *         when the updates were provided in a wrong order
     * @throws UpdateNullException
     *         When an update is null
     * @throws UpdateStepFailedException
     *         When a single update step failed.
     */
    public void onUpgrade(StorageToUpdate storageToUpdate, int oldVersion, int newVersion) throws UpdateFailedException, UpdateOrderWrongException, UpdateNullException, UpdateStepFailedException {
        if(oldVersion == newVersion){
            return; //nothing to do, db up to date
        }

        final int latestUpdateVersion = updatable.getLatestUpdateVersion(storageToUpdate);
        if (latestUpdateVersion != newVersion) {
            throw new UpdateFailedException("Latest update version != new Storage Version! UpdatePool incompatible with newest Storage version! latestUpdateVersion[" + latestUpdateVersion + "] <= newVersion[" + newVersion + "]");
        }

        final Collection<UpdateImpl> updateFactories = updatable.createUpdates();

        if(updateFactories.isEmpty()){
            throw new UpdateFailedException("Can't start updates with empty update collection!");
        }

        //check for correct update order
        final OrderResult orderResult = createOrderResultOf(updateFactories);
        orderResult.throwIfCorrupted();

        int lastVersionUpdate = 0;
        for (UpdateImpl update : updateFactories) {
            if (updatable.isClosed(storageToUpdate)) {
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
     * @param <UpdateImpl>
     *         The implementation of the update
     * @param updates
     *         The updates for checking the correct order.
     *
     * @return {@link OrderResult#forCorrect()} when the order is correct, {@link OrderResult#forWrong(int, int)} when the order is wrong, {@link OrderResult#forEqualVersions(int)}
     * when updates with the same update version were found
     *
     * @throws UpdateNullException
     *         When an update is null
     */
    public static <UpdateImpl extends Update> OrderResult createOrderResultOf(Collection<UpdateImpl> updates) throws UpdateNullException {
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
                return OrderResult.forWrong(versionFrom, versionTo);
            }
            if (versionFrom == versionTo) {
                return OrderResult.forEqualVersions(versionFrom);
            }
            //remember for next update version check
            previousCheckVersion = versionFrom = versionTo;
        }
        return OrderResult.forCorrect();
    }

    public static class OrderResult {

        public static OrderResult forWrong(int versionFrom, int toVersion) {
            OrderResult orderResult = new OrderResult();
            orderResult.wrongFromVersion = versionFrom;
            orderResult.wrongToVersion = toVersion;
            orderResult.type = Type.WRONG;
            return orderResult;
        }

        public static OrderResult forEqualVersions(int versionFrom) {
            OrderResult orderResult = new OrderResult();
            orderResult.wrongToVersion = orderResult.wrongFromVersion = versionFrom;
            orderResult.type = Type.EQUALS;
            return orderResult;
        }

        public static OrderResult forCorrect() {
            OrderResult orderResult = new OrderResult();
            orderResult.type = Type.CORRECT;
            return orderResult;
        }

        private enum Type {CORRECT, WRONG, EQUALS}

        private int wrongFromVersion;

        private int  wrongToVersion;
        private Type type;

        private OrderResult() {
        }

        public boolean isCorrect() {
            return type == Type.CORRECT;
        }

        public boolean isWrong() {
            return type == Type.WRONG;
        }

        public boolean hasEqualVersions() {
            return type == Type.EQUALS;
        }

        /**
         * Use this method to interpret the {@link OrderResult} and throw an exception if the order is not correct. Incorrect orders could cause corrupted data inside the storage.
         *
         * @throws UpdateOrderWrongException
         *         When this {@link OrderResult} is incorrect.
         */
        public void throwIfCorrupted() throws UpdateOrderWrongException {
            if (isWrong()) {
                throw UpdateOrderWrongException.forWrongOrderedVersions(wrongFromVersion, wrongToVersion);
            }
            if (hasEqualVersions()) {
                throw UpdateOrderWrongException.forEqualVersions(wrongFromVersion);
            }
        }
    }
}
