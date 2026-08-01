package com.kubemanager.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode roleNotFound, String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

}