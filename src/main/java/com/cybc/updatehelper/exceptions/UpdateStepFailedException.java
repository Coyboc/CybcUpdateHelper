package com.cybc.updatehelper.exceptions;

import com.cybc.updatehelper.Update;

/**
 * Thrown when a single update failed.
 */
public class UpdateStepFailedException extends RuntimeException {

    /**
     * The failed update or null
     */
    public final Update failedUpdate;

    public UpdateStepFailedException(Update update, Throwable throwable) {
        super("Update with version '" + update.getUpdateVersion() + "' failed!", throwable);
        this.failedUpdate = update;
    }

}
