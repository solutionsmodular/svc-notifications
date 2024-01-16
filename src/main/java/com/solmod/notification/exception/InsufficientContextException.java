package com.solmod.notification.exception;

import java.util.HashSet;
import java.util.Set;

/**
 * There are situations where context data is needed during the processing of notifications. If, during this processing,
 * the context does not have all needed keys, empty/null will NOT be assumed. Missing properties should throw this
 * Exception
 */
public class InsufficientContextException extends Exception {
    private Set<String> missingContextFields = new HashSet<>();
    public InsufficientContextException() {
        super();
    }

    public InsufficientContextException(String message) {
        super(message);
    }

    public void addMissingField(String fieldName) {
        missingContextFields.add(fieldName);
    }

    public Set<String> getMissingContextFields() {
        return missingContextFields;
    }
}
