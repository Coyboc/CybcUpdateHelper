package com.cybc.updatehelper.exceptions;

/**
 * Thrown for failed updates.
 */
public class UpdateFailedException extends RuntimeException {

    public UpdateFailedException(String detailMessage) {
        super(detailMessage);
    }

}
