package com.cybc.updatehelper.testing.exceptions;

import com.cybc.updatehelper.Update;
import com.cybc.updatehelper.UpdateWorker;

/**
 * Thrown for failed update tests.
 *
 * @see UpdateWorker#onPostUpdate(Object, Update)
 */
public class TestUpdateExecutionFailedException extends RuntimeException {

    public TestUpdateExecutionFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TestUpdateExecutionFailedException(String detailMessage) {
        super(detailMessage);
    }
}
