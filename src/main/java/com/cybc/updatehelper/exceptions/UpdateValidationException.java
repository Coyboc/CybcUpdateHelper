package com.cybc.updatehelper.exceptions;

import com.cybc.updatehelper.UpdateHelper;

/**
 * Is thrown if the order of the updates are incorrect.
 *
 * @see UpdateHelper.UpdateValidationResult
 */
public class UpdateValidationException extends RuntimeException {

    public static UpdateValidationException forEqualVersions(final int version) {
        return new UpdateValidationException("Found more then one versions for '" + version + "'");
    }

    public static UpdateValidationException forWrongOrderedVersions(final int fromVersion, final int toVersion) {
        return new UpdateValidationException("Wrong update order from version '" + fromVersion + "' to '" + toVersion + "'");
    }
    public static UpdateValidationException forEmpty() {
        return new UpdateValidationException("There are no updates provided!");
    }
    public static UpdateValidationException forWrongFinalVersion(final int expectedFinalVersion, final int actualFinalVersion) {
        return new UpdateValidationException("The provided updates are not sufficient to push the storage to the expected version. Expcted: " + expectedFinalVersion + ", actual: " + actualFinalVersion);
    }

    private UpdateValidationException(String detailMessage) {
        super(detailMessage);
    }

}
