package com.kubemanager.exception;

public class BadRequestException extends BaseException {

    public BadRequestException(ErrorCode invalidUserProfile, String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

}