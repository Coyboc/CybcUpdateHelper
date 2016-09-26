package com.cybc.updatehelper.exceptions;

/**
 * Thrown for failed updates.
 */
public class UpdateNullException extends RuntimeException {

    public UpdateNullException(String detailMessage) {
        super(detailMessage);
    }

}
