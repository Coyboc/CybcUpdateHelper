package com.cybc.updatehelper.exceptions;

/**
 * Is thrown if the order of the updates are incorrect.
 *
 * @see com.cybc.updatehelper.UpdateHelperBase.OrderResult
 */
public class UpdateOrderWrongException extends RuntimeException {

    public static UpdateOrderWrongException forEqualVersions(final int version) {
        return new UpdateOrderWrongException("Found more then one versions for '" + version + "'");
    }

    public static UpdateOrderWrongException forWrongOrderedVersions(final int fromVersion, final int toVersion) {
        return new UpdateOrderWrongException("Wrong update order from version '" + fromVersion + "' to '" + toVersion + "'");
    }

    private UpdateOrderWrongException(String detailMessage) {
        super(detailMessage);
    }

}
