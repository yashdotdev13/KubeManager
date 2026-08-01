package com.kubemanager.exception;

public class ConflictException extends BaseException {

    public ConflictException(ErrorCode usernameAlreadyExists, String message) {
        super(ErrorCode.CONFLICT, message);
    }

}