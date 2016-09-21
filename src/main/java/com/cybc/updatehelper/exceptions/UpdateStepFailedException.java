package com.cybc.updatehelper.exceptions;

import com.cybc.updatehelper.Update;

/**
 * Thrown when a single update failed.
 */
public class UpdateStepFailedException extends RuntimeException {

    /**
     * The failed update or null
     */
    public final Update failedUpdateOrNull;

    public UpdateStepFailedException(Update failedUpdateOrNull, Throwable throwable) {
        super(createErrorMessage(failedUpdateOrNull), throwable);
        this.failedUpdateOrNull = failedUpdateOrNull;
    }

    private static String createErrorMessage(Update update) {
        if (update == null) {
            return "Update is Null!";
        }
        return "Update with version '" + update.getUpdateVersion() + "' Failed!";
    }
}
