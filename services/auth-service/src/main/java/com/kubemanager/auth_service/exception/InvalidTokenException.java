package com.kubemanager.auth_service.exception;


import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

}